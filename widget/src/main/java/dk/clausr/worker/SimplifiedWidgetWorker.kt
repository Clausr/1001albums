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
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dk.clausr.core.common.model.doOnFailure
import dk.clausr.core.common.model.doOnSuccess
import dk.clausr.core.data.repository.OagRepository
import dk.clausr.core.data_widget.AlbumWidgetDataDefinition
import dk.clausr.core.data_widget.SerializedWidgetState
import dk.clausr.widget.AlbumCoverWidget2
import dk.clausr.widget.SimplifiedAlbumWidget
import java.time.Duration

@HiltWorker
class SimplifiedWidgetWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted private val workerParameters: WorkerParameters,
    private val oagRepository: OagRepository,

) : CoroutineWorker(appContext, workerParameters) {

    override suspend fun doWork(): Result {
        var workerResult: Result = Result.retry()
        val dataStore = AlbumWidgetDataDefinition.getDataStore(appContext)

        var projectId: String? = null
        // Don't show a loading spinner if we already have an album, that way it shouldn't flash
        dataStore.updateData { oldState ->
            when (oldState) {
                is SerializedWidgetState.Success -> {
                    projectId = oldState.currentProjectId
                    oldState
                }

                is SerializedWidgetState.Loading -> {
                    projectId = oldState.currentProjectId
                    oldState // Don't show loading thing when updating
//                    SerializedWidgetState.Loading(oldState.currentProjectId)
                }

                is SerializedWidgetState.Error -> {
                    projectId = oldState.currentProjectId
                    SerializedWidgetState.Error(
                        oldState.message,
                        currentProjectId = oldState.currentProjectId
                    )
                }

                is SerializedWidgetState.NotInitialized -> {
                    projectId = null
                    SerializedWidgetState.NotInitialized
                }
            }
        }

        projectId?.let { nnProjectId ->
            oagRepository.updateProject(nnProjectId)
                .doOnSuccess {
                    workerResult = Result.success()
                }
                .doOnFailure { _, _ ->
                    workerResult = Result.failure()
                }
        } ?: run {
            workerResult = Result.failure()
        }

        SimplifiedAlbumWidget.updateAll(appContext)
        AlbumCoverWidget2().updateAll(appContext)

        return workerResult
    }

    companion object {
        private const val simplifiedWorkerUniqueName = "simplifiedWorkerUniqueName"

        private fun startSingle() =
            OneTimeWorkRequestBuilder<SimplifiedWidgetWorker>()
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .addTag("SingleWorkForSimplified")
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                ).build()

        fun enqueueUnique(context: Context) {
            WorkManager.getInstance(context).enqueueUniqueWork(
                simplifiedWorkerUniqueName, ExistingWorkPolicy.REPLACE, startSingle()
            )
        }


        private const val PERIODIC_SYNC = "SimplifiedPeriodicSyncWorker"

        private val periodicConstraints =
            Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true).build()

        private fun periodicWorkSync() = PeriodicWorkRequestBuilder<SimplifiedWidgetWorker>(
            repeatInterval = Duration.ofHours(1)
        )
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, Duration.ofMinutes(10))
            .setConstraints(periodicConstraints).addTag("Periodic").build()

        fun start(
            context: Context,
            policy: ExistingPeriodicWorkPolicy = ExistingPeriodicWorkPolicy.UPDATE,
        ) {
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                PERIODIC_SYNC, policy, periodicWorkSync()
            )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(PERIODIC_SYNC)
        }
    }
}
