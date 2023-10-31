package dk.clausr.worker

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dk.clausr.core.data.repository.OagRepository
import dk.clausr.widget.DailyAlbumWidget
import java.util.concurrent.TimeUnit

@HiltWorker
class UpdateWidgetWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val oagRepository: OagRepository
) : CoroutineWorker(appContext, workerParams) {

    private val manager: GlanceAppWidgetManager by lazy {
        GlanceAppWidgetManager(appContext)
    }

    override suspend fun doWork(): Result {
        val projectId = inputData.getString(ProjectId) ?: return Result.failure()
        val widgetId = inputData.getInt(WidgetId, -1)
        if (widgetId == -1) return Result.failure()

        oagRepository.updateDailyAlbum(projectId)

        val widget = oagRepository.getWidget(projectId)

        return if (widget == null) {
            Result.retry()
        } else {
            val widgetGlanceId = manager.getGlanceIdBy(appWidgetId = widgetId)

            DailyAlbumWidget().update(appContext, widgetGlanceId)

//            delay(20_000)
//            WorkManager.getInstance(appContext)
//                .enqueueUniqueWork(
//                    "updateWidgetWork",
//                    ExistingWorkPolicy.KEEP,
//                    doSomething(projectId, widgetId))

            Result.success(
                Data.Builder()
                    .putString("PROJECT", widget.currentCoverUrl)
                    .build()
            )
        }
    }

    companion object {
        private const val ProjectId = "PROJECT_ID"
        private const val WidgetId = "WIDGET_ID"
        fun refreshAlbumRepeatedly(projectId: String, widgetId: Int) = PeriodicWorkRequestBuilder<UpdateWidgetWorker>(30, TimeUnit.MINUTES)
            .setInputData(
                Data.Builder()
                    .putString(ProjectId, projectId)
                    .putInt(WidgetId, widgetId)
                    .build()
            )
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()

        fun doSomething(projectId: String, widgetId: Int) = OneTimeWorkRequestBuilder<UpdateWidgetWorker>()
            .setInputData(
                Data.Builder()
                    .putString(ProjectId, projectId)
                    .putInt(WidgetId, widgetId)
                    .build()
            )
            .addTag("AlbumWidgetWorkThing")
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .setBackoffCriteria(backoffPolicy = BackoffPolicy.LINEAR, backoffDelay = 10_000, TimeUnit.SECONDS)
            .build()
    }
}
