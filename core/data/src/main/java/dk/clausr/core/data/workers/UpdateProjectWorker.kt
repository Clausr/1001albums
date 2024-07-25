package dk.clausr.core.data.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dk.clausr.core.common.model.doOnFailure
import dk.clausr.core.common.model.doOnSuccess
import dk.clausr.core.data.repository.OagRepository
import dk.clausr.core.network.NetworkError
import java.util.concurrent.TimeUnit

@HiltWorker
class UpdateProjectWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted private val workerParameters: WorkerParameters,
    private val oagRepository: OagRepository,
) : CoroutineWorker(appContext, workerParameters) {
    override suspend fun doWork(): Result {
        if (runAttemptCount >= MAX_RETRIES) return Result.failure(workDataOf("error" to "Max retries"))
        val projectId = workerParameters.inputData.getString(PROJECT_ID_KEY) ?: return Result.failure()

        var workerResult: Result = Result.retry()

        oagRepository.updateProject(projectId)
            .doOnSuccess {
                val isLatestAlbumRated = oagRepository.isLatestAlbumRated()

                workerResult = if (isLatestAlbumRated) {
                    Result.success()
                } else {
                    Result.retry()
                }
            }
            .doOnFailure {
                workerResult = when (it) {
                    is NetworkError.Generic -> Result.failure()
                    is NetworkError.ProjectNotFound -> Result.failure(workDataOf("Error" to "Project not found"))
                    is NetworkError.TooManyRequests -> Result.retry()
                }
            }

        return workerResult
    }

    companion object {
        const val PROJECT_ID_KEY = "ProjectIdKey"
        const val MAX_RETRIES = 10
        const val UNIQUE_WORK_NAME = "UpdateProject"
        private const val BACKOFF_SECONDS_DELAY = 30L

        private fun createWorkRequestBuilder(projectId: String): OneTimeWorkRequest.Builder {
            return OneTimeWorkRequestBuilder<UpdateProjectWorker>()
//                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .setInputData(workDataOf(PROJECT_ID_KEY to projectId))
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build(),
                )
                .setBackoffCriteria(BackoffPolicy.LINEAR, BACKOFF_SECONDS_DELAY, TimeUnit.SECONDS)
                .setInitialDelay(BACKOFF_SECONDS_DELAY, TimeUnit.SECONDS)
        }

        fun createOneTimeWorkRequest(projectId: String): OneTimeWorkRequest {
            return createWorkRequestBuilder(projectId).build()
        }

        fun run(
            context: Context,
            projectId: String,
        ) {
            WorkManager.getInstance(context)
                .enqueueUniqueWork(
                    UNIQUE_WORK_NAME,
                    ExistingWorkPolicy.REPLACE,
                    createOneTimeWorkRequest(projectId),
                )
        }
    }
}
