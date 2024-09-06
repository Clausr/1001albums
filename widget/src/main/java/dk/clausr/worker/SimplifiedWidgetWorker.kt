package dk.clausr.worker

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
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
import dk.clausr.widget.AlbumCoverWidget
import kotlinx.coroutines.flow.firstOrNull
import timber.log.Timber
import java.time.Duration

@HiltWorker
class SimplifiedWidgetWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted private val workerParameters: WorkerParameters,
    private val oagRepository: OagRepository,
    private val notificationRepository: NotificationRepository,

) : CoroutineWorker(appContext, workerParameters) {

    override suspend fun doWork(): Result {
        var workerResult: Result = Result.retry()
        val dataStore = AlbumWidgetDataDefinition.getDataStore(appContext)
        val projectId: String? = dataStore.data.firstOrNull()?.projectId

        // TODO Look into this actually checking if yesterdays album is rated

        projectId?.let {
            notificationRepository.updateNotifications(projectId)
            oagRepository.updateProject(projectId)
                .doOnSuccess {
                    workerResult = Result.success()
                }
                .doOnFailure { _ ->
                    workerResult = Result.failure()
                }
        } ?: run {
            workerResult = Result.failure(workDataOf("error" to "No project id set"))
        }

        AlbumCoverWidget().updateAll(appContext)

        return workerResult
    }

    companion object {
        private const val SIMPLIFIED_WORKER_UNIQUE_NAME = "simplifiedWorkerUniqueName"

        private fun startSingle() = OneTimeWorkRequestBuilder<SimplifiedWidgetWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .addTag("SingleWorkForSimplified")
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build(),
            )
            .build()

        fun enqueueUnique(context: Context) {
            WorkManager.getInstance(context).enqueueUniqueWork(
                SIMPLIFIED_WORKER_UNIQUE_NAME,
                ExistingWorkPolicy.KEEP,
                startSingle(),
            )
        }

        private const val PERIODIC_SYNC = "SimplifiedPeriodicSyncWorker"

        private val periodicConstraints =
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

        private fun periodicWorkSync() = PeriodicWorkRequestBuilder<SimplifiedWidgetWorker>(
            repeatInterval = Duration.ofHours(1),
        )
            .setBackoffCriteria(
                backoffPolicy = BackoffPolicy.EXPONENTIAL,
                duration = Duration.ofMinutes(10),
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
