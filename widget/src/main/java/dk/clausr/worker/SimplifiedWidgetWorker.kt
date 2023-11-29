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
import dk.clausr.core.data.repository.OagRepository
import dk.clausr.core.data_widget.AlbumWidgetDataDefinition
import dk.clausr.core.data_widget.SerializedWidgetState.Loading
import dk.clausr.core.data_widget.SerializedWidgetState.NotInitialized
import dk.clausr.core.data_widget.SerializedWidgetState.Success
import dk.clausr.widget.SimplifiedAlbumWidget
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.time.Duration

@HiltWorker
class SimplifiedWidgetWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted private val workerParameters: WorkerParameters,
    private val oagRepository: OagRepository,
) : CoroutineWorker(appContext, workerParameters) {

    override suspend fun doWork(): Result {
        val workerResult: Result
        val dataStore = AlbumWidgetDataDefinition.getDataStore(appContext)

        // Don't show a loading spinner if we already have an album, that way it shouldn't flash
        dataStore.updateData { oldState ->
            when (oldState) {
                is Success -> oldState
                else -> Loading(oldState.projectId)
            }
        }
        SimplifiedAlbumWidget.updateAll(appContext)

        val projectId: String? = runBlocking { oagRepository.projectId.first() }

        if (projectId == null) {
            dataStore.updateData {
                NotInitialized
            }
            workerResult = Result.failure()
        } else {
            val project = oagRepository.getProject(projectId)

            workerResult = if (project == null) {
                Result.retry()
            } else {
                Result.success()
            }
        }

        SimplifiedAlbumWidget.updateAll(appContext)

        return workerResult
    }

    companion object {
        private const val simplifiedWorkerUniqueName = "simplifiedWorkerUniqueName"

        fun startSingle() =
            OneTimeWorkRequestBuilder<SimplifiedWidgetWorker>().setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .addTag("SingleWorkForSimplified").setConstraints(
                    Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
                ).build()

        fun enqueueUnique(context: Context) {
            WorkManager.getInstance(context).enqueueUniqueWork(
                simplifiedWorkerUniqueName, ExistingWorkPolicy.REPLACE, startSingle()
            )
        }

        private const val periodicSync = "SimplifiedPeriodicSyncWorker"

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
                periodicSync, policy, periodicWorkSync()
            )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(periodicSync)
        }
    }
}
