package dk.clausr.worker

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.ForegroundInfo
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dk.clausr.widget.AlbumCoverWidget
import dk.clausr.worker.helper.OagNotificationType
import dk.clausr.worker.helper.syncForegroundInfo

@HiltWorker
class UpdateWidgetStateWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted private val workerParameters: WorkerParameters,
) : CoroutineWorker(appContext, workerParameters) {

    override suspend fun getForegroundInfo(): ForegroundInfo = appContext.syncForegroundInfo(OagNotificationType.UpdateWidgetState)
    override suspend fun doWork(): Result {
        AlbumCoverWidget().updateAll(appContext)

        return Result.success()
    }

    companion object {
        private const val UPDATE_WIDGET_UNIQUE_NAME = "UpdateWidgetStateWorker"

        private fun startSingle() = OneTimeWorkRequestBuilder<UpdateWidgetStateWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .build()

        fun enqueueUnique(context: Context) {
            WorkManager.getInstance(context)
                .enqueueUniqueWork(
                    UPDATE_WIDGET_UNIQUE_NAME,
                    ExistingWorkPolicy.REPLACE,
                    startSingle(),
                )
        }
    }
}
