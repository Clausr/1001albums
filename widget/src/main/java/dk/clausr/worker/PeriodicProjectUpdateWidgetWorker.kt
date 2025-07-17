package dk.clausr.worker

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.ForegroundInfo
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
import dk.clausr.core.common.network.Dispatcher
import dk.clausr.core.common.network.OagDispatchers
import dk.clausr.core.data.repository.NotificationRepository
import dk.clausr.core.data.repository.OagRepository
import dk.clausr.core.data_widget.AlbumWidgetDataDefinition
import dk.clausr.core.data_widget.SerializedWidgetState.Companion.projectId
import dk.clausr.core.network.NetworkError
import dk.clausr.widget.AlbumCoverWidget
import dk.clausr.worker.helper.OagNotificationType
import dk.clausr.worker.helper.isUniqueWorkerRunning
import dk.clausr.worker.helper.syncForegroundInfo
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.firstOrNull
import timber.log.Timber
import java.time.Duration

@HiltWorker
class PeriodicProjectUpdateWidgetWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted private val workerParameters: WorkerParameters,
    @Dispatcher(OagDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
    private val oagRepository: OagRepository,
    private val notificationRepository: NotificationRepository,
) : CoroutineWorker(appContext, workerParameters) {

    override suspend fun getForegroundInfo(): ForegroundInfo = appContext.syncForegroundInfo(oagNotificationType = OagNotificationType.PeriodicSync)

    override suspend fun doWork(): Result {
        if (runAttemptCount >= MAX_RETRIES) return Result.failure()
        Timber.d("Periodic run attempt $runAttemptCount")

        val isBurstRunning = appContext.isUniqueWorkerRunning(BurstUpdateWorker.UNIQUE_NAME)
        if (isBurstRunning) {
            Timber.i("Burst update is already running, no need for this")
            return Result.failure()
        }

        var workerResult: Result = Result.failure()
        val dataStore = AlbumWidgetDataDefinition.getDataStore(appContext)
        val projectId: String? = dataStore.data.firstOrNull()?.projectId

        Timber.i("PeriodicProjectUpdateWidgetWorker doing work for $projectId")
        projectId?.let {
            coroutineScope {
                val updateNotificationsAsync = async(ioDispatcher) { updateNotifications(it) }
                val updateProjectAsync = async(ioDispatcher) {
                    oagRepository.updateProject(projectId)
                }

                val updateProjectResult = updateProjectAsync.await()

                updateProjectResult
                    .doOnSuccess {
                        Timber.i("Project updated successfully")
                        updateNotificationsAsync.await()
                        workerResult = Result.success()
                    }
                    .doOnFailure {
                        workerResult = if (it is NetworkError.TooManyRequests) {
                            Result.retry()
                        } else {
                            Result.failure()
                        }
                    }
            }
        } ?: run {
            Timber.e("No project id set")
            workerResult = Result.failure(workDataOf("error" to "No project id set"))
        }

        Timber.d(
            "Periodic Worker result: ${
                when (workerResult) {
                    Result.success() -> "Success"
                    Result.retry() -> "Retry"
                    Result.failure() -> "Failure"
                    else -> "Unknown"
                }
            }",
        )
        AlbumCoverWidget().updateAll(context = appContext)
        return workerResult
    }

    private suspend fun updateNotifications(projectId: String) {
        notificationRepository.updateNotifications(
            origin = "PeriodicProjectUpdateWidgetWorker",
            projectId = projectId,
        )
    }

    companion object {
        const val SIMPLIFIED_WORKER_UNIQUE_NAME = "simplifiedWorkerUniqueName"
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

        private const val HALF_HOUR_MINUTES = 30L
        private fun periodicWorkSync() = PeriodicWorkRequestBuilder<PeriodicProjectUpdateWidgetWorker>(
            repeatInterval = Duration.ofMinutes(HALF_HOUR_MINUTES),
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
