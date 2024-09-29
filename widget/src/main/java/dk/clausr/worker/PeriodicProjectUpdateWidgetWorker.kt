package dk.clausr.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dk.clausr.core.common.model.doOnFailure
import dk.clausr.core.common.model.doOnSuccess
import dk.clausr.core.data.repository.NotificationRepository
import dk.clausr.core.data.repository.OagRepository
import dk.clausr.core.data_widget.AlbumWidgetDataDefinition
import dk.clausr.core.data_widget.SerializedWidgetState.Companion.projectId
import dk.clausr.core.network.NetworkError
import kotlinx.coroutines.flow.firstOrNull
import timber.log.Timber
import java.time.Duration

@HiltWorker
class PeriodicProjectUpdateWidgetWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted private val workerParameters: WorkerParameters,
    private val oagRepository: OagRepository,
    private val notificationRepository: NotificationRepository,
) : CoroutineWorker(appContext, workerParameters) {

    override suspend fun doWork(): Result {
        if (runAttemptCount >= MAX_RETRIES) return Result.failure()
        Timber.d("Periodic run attempt $runAttemptCount")
        var workerResult: Result = Result.failure()
        val dataStore = AlbumWidgetDataDefinition.getDataStore(appContext)
        val projectId: String? = dataStore.data.firstOrNull()?.projectId

        Timber.i("PeriodicProjectUpdateWidgetWorker doing work for $projectId")
        projectId?.let {
            notificationRepository.updateNotifications(
                origin = "PeriodicProjectUpdateWidgetWorker",
                projectId = projectId,
            )

            oagRepository.updateProject(projectId)
                .doOnSuccess {
                    Timber.i("Project updated successfully")
                    UpdateWidgetStateWorker.enqueueUnique(appContext)
                    workerResult = Result.success()
                }
                .doOnFailure {
                    workerResult = if (it is NetworkError.TooManyRequests) {
                        Result.retry()
                    } else {
                        Result.failure()
                    }
                }
        } ?: run {
            Timber.e("No project id set")
            workerResult = Result.failure(workDataOf("error" to "No project id set"))
        }

        Timber.d(
            "Periodic Worker result: ${
                when {
                    workerResult == Result.success() -> "Success"
                    workerResult == Result.retry() -> "Retry"
                    workerResult == Result.failure() -> "Failure"
                    else -> "Unknown"
                }
            }",
        )
        return workerResult
    }

    companion object {
        private const val SIMPLIFIED_WORKER_UNIQUE_NAME = "simplifiedWorkerUniqueName"
        const val MAX_RETRIES = 10
        private const val BACKOFF_SECONDS_DELAY = 30L

        private fun startSingle() = OneTimeWorkRequestBuilder<PeriodicProjectUpdateWidgetWorker>()
            .addTag("SingleWorkForPeriodicProjectUpdateWidgetWorker")
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build(),
            )
            .build()

        fun enqueueUnique(context: Context) {
            WorkManager.getInstance(context).enqueueUniqueWork(
                SIMPLIFIED_WORKER_UNIQUE_NAME,
                ExistingWorkPolicy.REPLACE,
                startSingle(),
            )
        }

        private const val PERIODIC_SYNC = "SimplifiedPeriodicSyncWorker"

        private val periodicConstraints =
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

        private fun periodicWorkSync() = PeriodicWorkRequestBuilder<PeriodicProjectUpdateWidgetWorker>(
            repeatInterval = Duration.ofHours(1),
        )
            .setBackoffCriteria(
                backoffPolicy = BackoffPolicy.LINEAR,
                duration = Duration.ofSeconds(BACKOFF_SECONDS_DELAY),
            )
            .setConstraints(periodicConstraints)
            .addTag("Periodic")
            .build()

        fun start(
            context: Context,
            policy: ExistingPeriodicWorkPolicy = ExistingPeriodicWorkPolicy.UPDATE,
        ) {
            Timber.d("Periodic update worker started")
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                PERIODIC_SYNC,
                policy,
                periodicWorkSync(),
            )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(PERIODIC_SYNC)
        }
    }
}
