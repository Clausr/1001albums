package dk.clausr.worker

import android.content.Context
import androidx.glance.appwidget.updateAll
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
import timber.log.Timber
import java.util.concurrent.TimeUnit

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
            DailyAlbumWidget().updateAll(appContext)

            Result.success()
        }
    }

    companion object {
        private const val ProjectId = "PROJECT_ID"

        fun refreshAlbumRepeatedly(projectId: String) = PeriodicWorkRequestBuilder<UpdateWidgetWorker>(30, TimeUnit.MINUTES)
            .setInputData(
                Data.Builder()
                    .putString(ProjectId, projectId)
                    .build()
            )
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()

        fun refreshOneTime(projectId: String) = OneTimeWorkRequestBuilder<UpdateWidgetWorker>()
            .setInputData(
                Data.Builder()
                    .putString(ProjectId, projectId)
                    .build()
            )
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .setBackoffCriteria(backoffPolicy = BackoffPolicy.LINEAR, backoffDelay = 10_000, TimeUnit.SECONDS)
            .build()
    }
}
