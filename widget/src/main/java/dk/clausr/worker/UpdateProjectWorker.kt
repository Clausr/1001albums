package dk.clausr.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dk.clausr.core.common.model.doOnFailure
import dk.clausr.core.common.model.doOnSuccess
import dk.clausr.core.data.repository.OagRepository

@HiltWorker
class UpdateProjectWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted private val workerParameters: WorkerParameters,
    private val oagRepository: OagRepository,
) : CoroutineWorker(appContext, workerParameters) {
    override suspend fun doWork(): Result {
        val projectId =
            workerParameters.inputData.getString(DATA_PROJECT_ID) ?: return Result.failure()

        var result: Result? = null
        oagRepository.updateProject(projectId)
            .doOnSuccess { result = Result.success() }
            .doOnFailure { _, _ ->
                result = Result.failure()
            }

        return result ?: Result.failure()
    }

    companion object {
        const val DATA_PROJECT_ID = "ProjectId"

        private fun startSingle(projectId: String) =
            OneTimeWorkRequestBuilder<UpdateProjectWorker>()
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .addTag("UpdateProjectWorker")
                .setInputData(
                    workDataOf(
                        DATA_PROJECT_ID to projectId,
                    )
                )
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                ).build()

        fun enqueueUnique(context: Context, projectId: String) {
            WorkManager.getInstance(context).enqueueUniqueWork(
                "UpdateProjectWorker", ExistingWorkPolicy.REPLACE, startSingle(projectId)
            )
        }
    }
}