package dk.clausr.a1001albumsgenerator.network.retrofit.notifications

import dk.LocalAssetManager
import dk.clausr.a1001albumsgenerator.network.NotificationsDataSource
import dk.clausr.a1001albumsgenerator.network.fake.FakeAssetManager
import dk.clausr.core.common.model.Result
import dk.clausr.core.common.network.Dispatcher
import dk.clausr.core.common.network.OagDispatchers
import dk.clausr.core.model.NotificationType
import dk.clausr.core.model.NotificationsResponse
import dk.clausr.core.network.NetworkError
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import javax.inject.Inject

@OptIn(ExperimentalSerializationApi::class)
class NotificationsMockDataSource @Inject constructor(
    private val json: Json,
    private val assets: FakeAssetManager = LocalAssetManager,
    @Dispatcher(OagDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) : NotificationsDataSource {
    override suspend fun getNotifications(
        projectId: String,
        showRead: Boolean,
    ): Result<NotificationsResponse, NetworkError> = withContext(ioDispatcher) {
        val response: NotificationsResponse = assets.open("mock_notification_response.json").use(json::decodeFromStream)
        val filteredNotifications = response.notifications
            .filterNot { it.type == NotificationType.Unknown }

        Result.Success(response.copy(notifications = filteredNotifications))
    }

    override suspend fun readAll(projectId: String): Result<Boolean, NetworkError> {
        return Result.Success(false)
    }
}
