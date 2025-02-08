package dk.clausr.core.data.repository

import androidx.datastore.core.DataStore
import dk.clausr.a1001albumsgenerator.network.NotificationsDataSource
import dk.clausr.core.common.model.doOnFailure
import dk.clausr.core.common.model.doOnSuccess
import dk.clausr.core.common.model.map
import dk.clausr.core.common.network.Dispatcher
import dk.clausr.core.common.network.OagDispatchers
import dk.clausr.core.data.model.notifications.asExternalModel
import dk.clausr.core.data.model.notifications.toEntities
import dk.clausr.core.data_widget.SerializedWidgetState
import dk.clausr.core.database.dao.NotificationDao
import dk.clausr.core.database.model.NotificationEntity
import dk.clausr.core.model.Notification
import dk.clausr.core.model.NotificationData
import dk.clausr.core.model.NotificationType
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
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
    private val widgetDataStore: DataStore<SerializedWidgetState>,
    private val userDataRepository: UserRepository,
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
                Timber.i("Notifications gotten correctly.")
                val nonUnknownNotifications = networkNotifications.notifications.filterNot { it.type == NotificationType.Unknown }
                if (networkNotifications.notifications.isEmpty()) {
                    notificationDao.readNotifications()
                } else {
                    notificationDao.insertNotifications(nonUnknownNotifications.toEntities())
                }

                // Update widget data
                widgetDataStore.updateData {
                    when (it) {
                        is SerializedWidgetState.Success ->
                            it.copy(data = it.data.copy(unreadNotifications = networkNotifications.notifications.size))

                        else -> it
                    }
                }
            }
            .doOnFailure {
                Timber.e(it.cause, "Notifications went wrong.. -- ${it.cause}")
            }
    }

    // When and where do I put this?
    suspend fun findGroupSlugFromNotifications(
        projectId: String,
        force: Boolean = false,
    ): String? {

        val currentGroupSlug = userDataRepository.userData.firstOrNull()?.groupSlug

        return if (currentGroupSlug == null || force) {
            // Do some other suspend thing

            val res = networkDataSource.getNotifications(
                projectId = projectId,
                showRead = true,
            )
                .map { response ->
                    response.notifications
                        .sortedBy(Notification::createdAt)
                        .firstNotNullOfOrNull { notification ->
                            when (val data = notification.data) {
                                is NotificationData.GroupAlbumsGeneratedData -> data.groupSlug
                                is NotificationData.GroupReviewData -> data.groupSlug
                                is NotificationData.NewGroupMemberData -> data.groupSlug
                                is NotificationData.AlbumsRatedData -> null
                                is NotificationData.ReviewThumbUpData -> null
                                null -> null
                            }
                        }
                }
                .doOnSuccess {
                    Timber.d("Latest group slug: $it")
                    userDataRepository.setGroupName(it)
                }
            res.getOrNull()
        } else {
            Timber.d("Latest group slug found in datastore: $currentGroupSlug")
            currentGroupSlug
        }

    }

    suspend fun readAll(projectId: String) = withContext(ioDispatcher) {
        // Save unread notifications so we're able to revert if endpoint fails
        val unreadNotificationIds = notificationDao.getAllUnreadNotifications().map { it.id }
        // Mark notifications as read locally
        notificationDao.readNotifications()

        networkDataSource.readAll(projectId)
            .doOnSuccess {
                // Update widget data
                widgetDataStore.updateData {
                    when (it) {
                        is SerializedWidgetState.Success ->
                            it.copy(data = it.data.copy(unreadNotifications = 0))

                        else -> it
                    }
                }
            }
            .doOnFailure {
                notificationDao.markNotificationsAsUnread(unreadNotificationIds)
                Timber.e(it.cause, "Could not mark notifications as read")
            }
    }
}
