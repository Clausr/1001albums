package dk.clausr.core.data.repository

import dk.clausr.core.common.network.Dispatcher
import dk.clausr.core.common.network.OagDispatchers
import dk.clausr.core.data.model.log.OagLog
import dk.clausr.core.database.dao.LogDao
import dk.clausr.core.database.model.LogEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoggingRepository @Inject constructor(
    @Dispatcher(OagDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
    private val logDao: LogDao,
) {
    suspend fun insertLog(
        message: String,
        logLevel: OagLog.LogLevel,
        tag: String,
    ) = withContext(ioDispatcher) {
        val excessLogs = logDao.getRowCount() - MAX_ITEMS
        if (excessLogs > 0) {
            logDao.deleteOldestLogs(excessLogs)
        }

        logDao.insertLog(
            LogEntity(
                message = message,
                level = logLevel.priorityConstant,
                tag = tag,
            ),
        )
    }

    fun getAllLogs(): Flow<List<OagLog>> {
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss z") // Customize the pattern as needed

        return logDao.getAllLogs().map { logs ->
            logs.map {
                val zonedDateTime = it.timestamp.atZone(ZoneId.systemDefault())
                val formattedDateTime = formatter.format(zonedDateTime)
                OagLog(
                    message = it.message,
                    level = OagLog.LogLevel.fromPriorityConstant(it.level),
                    tag = it.tag,
                    timestamp = it.timestamp,
                    dateTime = formattedDateTime,
                )
            }
        }
    }

    companion object {
        const val MAX_ITEMS = 1000
    }
}
