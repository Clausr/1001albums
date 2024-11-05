package dk.clausr.core.data.model.log

import android.util.Log
import java.time.Instant

data class OagLog(
    val message: String,
    val level: LogLevel,
    val tag: String,
    val timestamp: Instant? = null,
) {
    enum class LogLevel(val priorityConstant: Int) {
        VERBOSE(Log.VERBOSE),
        DEBUG(Log.DEBUG),
        INFO(Log.INFO),
        WARN(Log.WARN),
        ERROR(Log.ERROR),
        ASSERT(Log.ASSERT),
        ;

        companion object {
            fun fromPriorityConstant(priorityConstant: Int): LogLevel {
                return when (priorityConstant) {
                    Log.VERBOSE -> VERBOSE
                    Log.DEBUG -> DEBUG
                    Log.INFO -> INFO
                    Log.WARN -> WARN
                    Log.ERROR -> ERROR
                    Log.ASSERT -> ASSERT
                    else -> error("No such priority..")
                }
            }
        }
    }
}
