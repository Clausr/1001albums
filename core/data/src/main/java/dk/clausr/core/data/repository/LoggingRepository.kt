package dk.clausr.core.data.repository

import dk.clausr.core.common.network.Dispatcher
import dk.clausr.core.common.network.OagDispatchers
import dk.clausr.core.data.model.log.OagLog
import dk.clausr.core.database.dao.LogDao
import dk.clausr.core.database.model.LogEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoggingRepository @Inject constructor(
    @Dispatcher(OagDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
    private val logDao: LogDao,
) {
    suspend fun insertLog(log: OagLog) = withContext(ioDispatcher) {
        logDao.insertLog(LogEntity(message = log.message, level = log.level.priorityConstant, tag = log.tag))
    }

    fun getAllLogs() = logDao.getAllLogs().map { logs ->
        logs.map {
            OagLog(
                message = it.message,
                level = OagLog.LogLevel.fromPriorityConstant(it.level),
                tag = it.tag,
                timestamp = it.timestamp,
            )
        }
    }
}
