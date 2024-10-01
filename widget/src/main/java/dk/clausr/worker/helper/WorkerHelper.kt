package dk.clausr.worker.helper

import android.content.Context
import androidx.work.WorkInfo
import androidx.work.WorkManager
import timber.log.Timber
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

fun Context.isUniqueWorkerRunning(uniqueWorkerId: String): Boolean {
    val workManager = WorkManager.getInstance(this)
    val workInfos = workManager.getWorkInfosForUniqueWork(uniqueWorkerId).get()

    if (workInfos.isNotEmpty()) {
        val workInfo = workInfos[0] // Get the first and only WorkInfo
        // Log important details about the WorkInfo
        Timber.i("WorkInfo for uniqueWorkerId: $uniqueWorkerId")
        Timber.i("State: ${workInfo.state}")
        Timber.i("Output Data: ${workInfo.outputData}")
        Timber.i("Run Attempt Count: ${workInfo.runAttemptCount}")
        Timber.i("Tags: ${workInfo.tags}")
        Timber.i("Id: ${workInfo.id}")
        Timber.i("Next Scheduled At: ${LocalDateTime.ofInstant(Instant.ofEpochMilli(workInfo.nextScheduleTimeMillis), ZoneId.systemDefault())}")
        val isRunning = when (workInfo.state) {
            WorkInfo.State.ENQUEUED,
            WorkInfo.State.RUNNING,
            -> true

            else -> false
        }
        Timber.i("Is Running: $isRunning")

        return isRunning
    }
    // If there's no work info, return false (not running)
    return false
}
