package dk.clausr.a1001albumsgenerator.utils

import android.util.Log
import dk.clausr.core.data.model.log.OagLog
import dk.clausr.core.data.repository.LoggingRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class RoomLoggingTree @Inject constructor(
    private val loggingRepository: LoggingRepository,
) : Timber.Tree() {
    override fun log(
        priority: Int,
        tag: String?,
        message: String,
        t: Throwable?,
    ) {
        val logLevel = OagLog.LogLevel.fromPriorityConstant(priority)

        if (priority < Log.DEBUG) return

        // Insert log into the database
        val logEntity = OagLog(
            message = message,
            level = logLevel,
            tag = tag.orEmpty(),
        )

        // Make sure to use a coroutine since Room operations are suspending
        CoroutineScope(Dispatchers.IO).launch {
            loggingRepository.insertLog(logEntity)
        }
    }
}
