package dk.clausr.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
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
import dk.clausr.core.network.NetworkError
import timber.log.Timber
import java.util.concurrent.TimeUnit

@HiltWorker
class BurstUpdateWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted private val workerParameters: WorkerParameters,
    private val oagRepository: OagRepository,
) : CoroutineWorker(appContext, workerParameters) {

    override suspend fun doWork(): Result {
        if (runAttemptCount >= MAX_RETRIES) return Result.failure()
        Timber.d("Burst do work retry no: $runAttemptCount ")

        val projectId =
            workerParameters.inputData.getString(PROJECT_ID_KEY) ?: return Result.failure()
        Timber.i("BurstUpdateWorker doing work for $projectId")

        var result: Result? = null

        oagRepository.updateProject(projectId)
            .doOnSuccess {
                val isLatestAlbumRated = oagRepository.isLatestAlbumRated()

                result = if (isLatestAlbumRated) {
                    UpdateWidgetStateWorker.enqueueUnique(appContext)
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

        return result ?: Result.failure()
    }

    companion object {
        const val PROJECT_ID_KEY = "ProjectIdKey"
        const val MAX_RETRIES = 10
        private const val BACKOFF_SECONDS_DELAY = 30L

        private fun enqueueBurstUpdate(projectId: String) = OneTimeWorkRequestBuilder<BurstUpdateWorker>()
            .setExpedited(RUN_AS_NON_EXPEDITED_WORK_REQUEST)
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
                    "BurstUpdateWorker",
                    ExistingWorkPolicy.KEEP,
                    enqueueBurstUpdate(projectId),
                )
        }
    }
}
