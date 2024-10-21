package dk.clausr.worker

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.ForegroundInfo
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dk.clausr.core.common.model.doOnFailure
import dk.clausr.core.common.model.doOnSuccess
import dk.clausr.core.data.repository.OagRepository
import dk.clausr.core.network.NetworkError
import dk.clausr.widget.AlbumCoverWidget
import dk.clausr.worker.helper.OagNotificationType
import dk.clausr.worker.helper.syncForegroundInfo
import timber.log.Timber
import java.util.concurrent.TimeUnit

@HiltWorker
class BurstUpdateWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted private val workerParameters: WorkerParameters,
    private val oagRepository: OagRepository,
) : CoroutineWorker(appContext, workerParameters) {

    override suspend fun getForegroundInfo(): ForegroundInfo = appContext.syncForegroundInfo(oagNotificationType = OagNotificationType.BurstSync)

    override suspend fun doWork(): Result {
        if (runAttemptCount >= MAX_RETRIES) return Result.failure()
        Timber.d("Burst do work retry no: $runAttemptCount ")

        val projectId =
            workerParameters.inputData.getString(PROJECT_ID_KEY) ?: return Result.failure()
        Timber.i("BurstUpdateWorker doing work for $projectId")

        var result: Result? = null

        oagRepository.updateProject(projectId)
            .doOnSuccess {
                result = if (oagRepository.isLatestAlbumRated()) {
                    AlbumCoverWidget().updateAll(context = appContext)
                    Result.success()
                } else {
                    Result.retry()
                }
            }
            .doOnFailure {
                if (it is NetworkError.TooManyRequests) {
                    result = Result.retry()
                }
            }

        Timber.d(
            "Burst Worker result: ${
                when (result) {
                    Result.success() -> "Success"
                    Result.retry() -> "Retry"
                    Result.failure() -> "Failure"
                    else -> "Unknown"
                }
            }",
        )

        return result ?: Result.failure()
    }

    companion object {
        const val PROJECT_ID_KEY = "ProjectIdKey"
        const val MAX_RETRIES = 10
        private const val BACKOFF_SECONDS_DELAY = 30L
        const val UNIQUE_NAME = "BurstUpdateWorker"

        private fun enqueueBurstUpdate(projectId: String) = OneTimeWorkRequestBuilder<BurstUpdateWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .addTag("BurstUpdateWorkerTag")
            .setInputData(
                workDataOf(
                    PROJECT_ID_KEY to projectId,
                ),
            )
            .setConstraints(
                Constraints
                    .Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build(),
            )
            .setBackoffCriteria(BackoffPolicy.LINEAR, BACKOFF_SECONDS_DELAY, TimeUnit.SECONDS)
            .build()

        fun enqueueUnique(
            context: Context,
            projectId: String,
        ) {
            WorkManager.getInstance(context)
                .enqueueUniqueWork(
                    UNIQUE_NAME,
                    ExistingWorkPolicy.REPLACE,
                    enqueueBurstUpdate(projectId),
                )
        }
    }
}
