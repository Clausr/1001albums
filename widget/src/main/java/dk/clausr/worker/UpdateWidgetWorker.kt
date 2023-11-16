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
import dk.clausr.core.data.repository.OagRepository
import dk.clausr.widget.DailyAlbumWidget
import timber.log.Timber
import java.time.Duration

@HiltWorker
class UpdateWidgetWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val oagRepository: OagRepository
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        val projectId = inputData.getString(ProjectId) ?: return Result.failure()
        Timber.d("Do Work with $projectId")

        // Get from backend
        oagRepository.updateDailyAlbum(projectId)

        val widget = oagRepository.getWidget(projectId)

        return if (widget == null) {
            Result.retry()
        } else {
            DailyAlbumWidget.updateAll(appContext)

            Result.success()
        }
    }

    companion object {
        private const val ProjectId = "PROJECT_ID"
        private const val updateDailyAlbumWorkerUniqueName = "updateDailyAlbumWorkerUniqueName"

        fun startSingle(projectId: String) = OneTimeWorkRequestBuilder<UpdateWidgetWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .setConstraints(
                Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
            )
            .setInputData(
                workDataOf(
                    ProjectId to projectId,
                )
            )
            .build()

        fun enqueueUnique(context: Context, projectId: String) {
            WorkManager.getInstance(context).enqueueUniqueWork(
                updateDailyAlbumWorkerUniqueName, ExistingWorkPolicy.KEEP, startSingle(projectId)
            )
        }

        private const val periodicSync = "PeriodicSyncWorker"

        private val periodicConstraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()

        private fun periodicWorkSync(projectId: String) =
            PeriodicWorkRequestBuilder<UpdateWidgetWorker>(repeatInterval = Duration.ofHours(2))
                .setInputData(workDataOf(ProjectId to projectId))
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, Duration.ofMinutes(10))
                .setInitialDelay(Duration.ofSeconds(10))
                .setConstraints(periodicConstraints)
                .build()

        fun start(
            context: Context,
            policy: ExistingPeriodicWorkPolicy = ExistingPeriodicWorkPolicy.UPDATE,
            projectId: String,
        ) {
            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    periodicSync,
                    policy,
                    periodicWorkSync(projectId)
                )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(periodicSync)
        }
//
//        fun refreshAlbumRepeatedly(projectId: String) = PeriodicWorkRequestBuilder<UpdateWidgetWorker>(15, TimeUnit.MINUTES)
//            .setInputData(
//                Data.Builder()
//                    .putString(ProjectId, projectId)
//                    .build()
//            )
//            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
//            .build()
//
//        fun refreshOneTime(projectId: String) = OneTimeWorkRequestBuilder<UpdateWidgetWorker>()
//            .setInputData(
//                Data.Builder()
//                    .putString(ProjectId, projectId)
//                    .build()
//            )
//            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
//            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
//            .setBackoffCriteria(backoffPolicy = BackoffPolicy.LINEAR, backoffDelay = 10_000, TimeUnit.SECONDS)
//            .build()
    }
}
