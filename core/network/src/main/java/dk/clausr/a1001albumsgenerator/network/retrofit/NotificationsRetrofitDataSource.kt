package dk.clausr.a1001albumsgenerator.network.retrofit

import dk.clausr.a1001albumsgenerator.network.NotificationsDataSource
import dk.clausr.a1001albumsgenerator.utils.doNetwork
import dk.clausr.core.common.model.Result
import dk.clausr.core.model.NotificationsResponse
import dk.clausr.core.network.NetworkError
import retrofit2.Retrofit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationsRetrofitDataSource @Inject constructor(
    retrofit: Retrofit,
) : NotificationsDataSource {
    private val api = retrofit.create(NotificationsApi::class.java)

    override suspend fun getNotifications(
        projectId: String,
        showRead: Boolean,
    ): Result<NotificationsResponse, NetworkError> = doNetwork {
        api.getNotifications(projectId = projectId, read = false)
    }

    override suspend fun readAll(projectId: String): Result<Boolean, NetworkError> = doNetwork {
        val result = api.readAll(projectId)
        return Result.Success(result.success)
    }
}
