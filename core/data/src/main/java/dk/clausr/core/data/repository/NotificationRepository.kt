package dk.clausr.core.data.repository

import dk.clausr.a1001albumsgenerator.network.NotificationsDataSource
import dk.clausr.core.common.model.doOnFailure
import dk.clausr.core.common.model.doOnSuccess
import dk.clausr.core.common.network.Dispatcher
import dk.clausr.core.common.network.OagDispatchers
import dk.clausr.core.data.model.notifications.asExternalModel
import dk.clausr.core.data.model.notifications.toEntities
import dk.clausr.core.database.dao.NotificationDao
import dk.clausr.core.database.model.NotificationEntity
import dk.clausr.core.model.Notification
import dk.clausr.core.model.NotificationType
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepository @Inject constructor(
    private val networkDataSource: NotificationsDataSource,
    @Dispatcher(OagDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
    private val notificationDao: NotificationDao,
) {
    val unreadNotifications: Flow<List<Notification>> = notificationDao.getUnreadNotifications()
        .map { entities ->
            entities.map(NotificationEntity::asExternalModel)
        }

    val notifications: Flow<List<Notification>> = notificationDao.getNotifications()
        .map { entities ->
            entities.map(NotificationEntity::asExternalModel)
        }

    suspend fun updateNotifications(
        origin: String,
        projectId: String,
        getRead: Boolean = false,
    ) {
        Timber.i("updateNotifications origin: $origin")
        networkDataSource.getNotifications(
            projectId = projectId,
            showRead = getRead,
        )
            .doOnSuccess { networkNotifications ->
                val nonUnknownNotifications = networkNotifications.notifications.filterNot { it.type == NotificationType.Unknown }
                if (networkNotifications.notifications.isEmpty()) {
                    notificationDao.readNotifications()
                } else {
                    notificationDao.insertNotifications(nonUnknownNotifications.toEntities())
                }
            }
            .doOnFailure {
                Timber.e(it.cause, "Notifications went wrong..")
            }
    }

    suspend fun readAll(projectId: String) = withContext(ioDispatcher) {
        networkDataSource.readAll(projectId)
            .doOnSuccess {
                notificationDao.readNotifications()
            }
            .doOnFailure {
                Timber.e(it.cause, "Could not mark notifications as read")
            }
    }
}
