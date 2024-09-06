package dk.clausr.a1001albumsgenerator.network

import dk.clausr.core.common.model.Result
import dk.clausr.core.model.NotificationsResponse
import dk.clausr.core.network.NetworkError

interface NotificationsDataSource {
    suspend fun getNotifications(projectId: String): Result<NotificationsResponse, NetworkError>
    suspend fun readAll(projectId: String): Result<Boolean, NetworkError>
}