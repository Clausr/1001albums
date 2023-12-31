package dk.clausr.worker

import android.content.Context
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
import kotlinx.coroutines.delay

@HiltWorker
class BurstUpdateWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted private val workerParameters: WorkerParameters,
    private val oagRepository: OagRepository,
) : CoroutineWorker(appContext, workerParameters) {

    override suspend fun doWork(): Result {
        val updatedProject = oagRepository.updateProject()
        var retryNumber = workerParameters.inputData.getInt(retryDataKey, 0)

        return if (updatedProject == null) {
            Result.failure()
        } else {
            if (retryNumber >= maxRetries) {
                Result.failure()
            } else if (updatedProject.history.lastOrNull()?.rating == Rating.Unrated) {
                retryNumber++
                delay(25_000)
                enqueueBurstUpdate(appContext, retryNumber)

                Result.failure()
            } else Result.success()
        }
    }

    companion object {
        const val retryDataKey = "RetryDataKey"
        const val maxRetries = 10
        fun enqueueBurstUpdate(context: Context, retryNumber: Int = 0) {
            val burstWorker = OneTimeWorkRequestBuilder<BurstUpdateWorker>().setExpedited(
                    RUN_AS_NON_EXPEDITED_WORK_REQUEST
                ).addTag("InitialBurstUpdateWorker$retryNumber")
                .setInputData(workDataOf(retryDataKey to retryNumber)).setConstraints(
                    Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
                ).build()


            WorkManager.getInstance(context).enqueue(burstWorker)
        }
    }
}
