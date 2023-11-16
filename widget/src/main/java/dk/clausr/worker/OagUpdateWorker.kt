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
import dk.clausr.core.model.AlbumWidgetData
import dk.clausr.data.AlbumWidgetDataDefinition
import dk.clausr.data.SerializedWidgetState
import dk.clausr.widget.DailyAlbumWidget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import java.time.Duration

@HiltWorker
class OagUpdateWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val oagRepository: OagRepository
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        lateinit var workerResult: Result

        val dataStore = AlbumWidgetDataDefinition.getDataStore(appContext)

        dataStore.updateData { oldData ->
            SerializedWidgetState.Loading(oldData.projectId)
        }

        DailyAlbumWidget.updateAll(appContext)

        Timber.d("New do work thing - before blocking call")
        val projectId = runBlocking { oagRepository.projectId.first() } ?: return Result.failure()

        val something = oagRepository.updateDailyAlbum(projectId)

        CoroutineScope(Dispatchers.IO).launch {
            oagRepository.widget.collectLatest {

            }
        }
        val widget = oagRepository.getWidget(projectId)

        if (widget != null) {
            dataStore.updateData { oldData ->
                SerializedWidgetState.Success(
                    AlbumWidgetData(widget.currentCoverUrl), currentProjectId = projectId
                )
            }
            workerResult = Result.success()
        }

        DailyAlbumWidget.updateAll(appContext)

        return workerResult
    }

    companion object {
        const val ProjectId = "ProjectID"
        private const val updateDailyAlbumWorkerUniqueName = "updateDailyAlbumWorkerUniqueName"

        fun startSingle(projectId: String) = OneTimeWorkRequestBuilder<OagUpdateWorker>()
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
            PeriodicWorkRequestBuilder<OagUpdateWorker>(repeatInterval = Duration.ofHours(2))
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
    }
}
