package dk.clausr.worker

import android.appwidget.AppWidgetManager
import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.updateAll
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dk.clausr.core.data.repository.OagRepository
import dk.clausr.widget.DailyAlbumWidget
import timber.log.Timber
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
        oagRepository.updateDailyAlbum(projectId)
        val widget = oagRepository.getWidget(projectId)


        // Discover the GlanceAppWidget
        val appWidgetManager = AppWidgetManager.getInstance(appContext)
        val receivers = appWidgetManager.installedProviders
            .filter { it.provider.packageName == appContext.packageName }
            .map { it.provider.className }

        receivers.mapNotNull { receiverName ->
            val receiverClass = Class.forName(receiverName)
            if (!GlanceAppWidgetReceiver::class.java.isAssignableFrom(receiverClass)) {
                return@mapNotNull null
            }
            val receiver = receiverClass.getDeclaredConstructor()
                .newInstance() as GlanceAppWidgetReceiver
            val provider = receiver.glanceAppWidget.javaClass

//            coroutineScope.launch {
            val sizes = manager.getGlanceIds(provider).flatMap { id ->
                manager.getAppWidgetSizes(id)
            }
            Timber.d("Sizes: ${sizes.joinToString { it.toString() }}")
            manager.getGlanceIds(provider).map { id ->
                Timber.d("GlanceId: $id")
            }
        }

        return if (widget == null) {
            Result.retry()
        } else {
            DailyAlbumWidget().updateAll(appContext)

            Result.success(
                Data.Builder()
                    .putString("PROJECT", widget.currentCoverUrl)
                    .build()
            )
        }
    }

    companion object {
        private const val ProjectId = "PROJECT_ID"
        fun refreshAlbumRepeatedly(projectId: String) = PeriodicWorkRequestBuilder<UpdateWidgetWorker>(15, TimeUnit.MINUTES)
            .setInputData(Data.Builder().putString(ProjectId, projectId).build())
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()
    }
}
