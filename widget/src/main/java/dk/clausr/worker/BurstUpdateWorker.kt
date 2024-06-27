package dk.clausr.worker

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dk.clausr.core.common.model.doOnFailure
import dk.clausr.core.common.model.doOnSuccess
import dk.clausr.core.data.repository.OagRepository
import dk.clausr.widget.AlbumCoverWidget
import java.util.concurrent.TimeUnit

@HiltWorker
class BurstUpdateWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted private val workerParameters: WorkerParameters,
    private val oagRepository: OagRepository,
) : CoroutineWorker(appContext, workerParameters) {

    private val project = oagRepository.project

    override suspend fun doWork(): Result {
        var retryNumber = workerParameters.inputData.getInt(retryDataKey, 0)
        // Prerequisites
        if (retryNumber > maxRetries) return Result.failure()
        val projectId =
            workerParameters.inputData.getString(projectIdKey) ?: return Result.failure()

        var result: Result? = null

        oagRepository.updateProject(projectId)
            .doOnSuccess {
                AlbumCoverWidget().updateAll(appContext)

                result = Result.success()
            }
            .doOnFailure { _, _ ->
                result = Result.retry()
            }

        return result ?: Result.failure()
    }

    companion object {
        const val retryDataKey = "RetryDataKey"
        const val projectIdKey = "ProjectIdKey"
        const val maxRetries = 10
        fun enqueueBurstUpdate(
            context: Context,
            retryNumber: Int = 0,
            projectId: String,
        ) {
            val burstWorker = OneTimeWorkRequestBuilder<BurstUpdateWorker>()
                .setExpedited(RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .addTag("InitialBurstUpdateWorker_$retryNumber")
                .setInputData(
                    workDataOf(
                        retryDataKey to retryNumber,
                        projectIdKey to projectId,
                    )
                )
                .setConstraints(
                    Constraints
                        .Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .setBackoffCriteria(BackoffPolicy.LINEAR, 30, TimeUnit.SECONDS)
                .build()


            WorkManager.getInstance(context).enqueue(burstWorker)
        }
    }
}
