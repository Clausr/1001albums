package dk.clausr.core.data.repository

import androidx.datastore.core.DataStore
import dk.clausr.a1001albumsgenerator.network.OAGDataSource
import dk.clausr.core.common.model.doOnFailure
import dk.clausr.core.common.model.doOnSuccess
import dk.clausr.core.common.network.Dispatcher
import dk.clausr.core.common.network.OagDispatchers
import dk.clausr.core.data.model.notifications.asExternalModel
import dk.clausr.core.data.model.notifications.toEntity
import dk.clausr.core.data_widget.SerializedWidgetState
import dk.clausr.core.database.dao.NotificationDao
import dk.clausr.core.model.NotificationResponse
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepository @Inject constructor(
    private val networkDataSource: OAGDataSource,
    @Dispatcher(OagDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
    private val widgetDataStore: DataStore<SerializedWidgetState>,
    private val notificationDao: NotificationDao,
) {

//    val notifications = notificationDao.getNotifications()

    val notifications: Flow<List<NotificationResponse>> = notificationDao.getNotifications().map { entities ->
        entities.map { it.asExternalModel() }
    }

    suspend fun updateNotifications(projectId: String) {
        networkDataSource.getNotifications(projectId)
            .doOnSuccess {
                notificationDao.insertNotifications(it.toEntity())
            }
            .doOnFailure {
                Timber.e("Notifications went wrong..")
            }
    }
}