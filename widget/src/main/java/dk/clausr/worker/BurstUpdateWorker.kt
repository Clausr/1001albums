package dk.clausr.worker

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dk.clausr.core.data.repository.OagRepository
import dk.clausr.core.model.Rating
import dk.clausr.widget.AlbumCoverWidget2
import dk.clausr.widget.SimplifiedAlbumWidget
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest

@HiltWorker
class BurstUpdateWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted private val workerParameters: WorkerParameters,
    private val oagRepository: OagRepository,
) : CoroutineWorker(appContext, workerParameters) {

    private val project = oagRepository.project

    override suspend fun doWork(): Result {
        var retryNumber = workerParameters.inputData.getInt(retryDataKey, 0)

        var res = Result.retry()

        project.collectLatest { updatedProject ->
            res = if (updatedProject == null) {
                Result.failure()
            } else {
                if (retryNumber >= maxRetries) {
                    Result.failure()
                } else if (updatedProject.history.lastOrNull()?.rating == Rating.Unrated) {
                    retryNumber++
                    delay(25_000)
//                    enqueueBurstUpdate(appContext, retryNumber)

                    Result.failure()
                } else {
                    Result.success()
                }
            }
        }

        oagRepository.updateProject()

        SimplifiedAlbumWidget.updateAll(appContext)
        AlbumCoverWidget2().updateAll(appContext)

        return res
    }

    companion object {
        const val retryDataKey = "RetryDataKey"
        const val maxRetries = 10
        fun enqueueBurstUpdate(context: Context, retryNumber: Int = 0) {
            val burstWorker = OneTimeWorkRequestBuilder<BurstUpdateWorker>()
                .setExpedited(RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .addTag("InitialBurstUpdateWorker_$retryNumber")
                .setInputData(workDataOf(retryDataKey to retryNumber))
                .setConstraints(
                    Constraints
                        .Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()


            WorkManager.getInstance(context).enqueue(burstWorker)
        }
    }
}
