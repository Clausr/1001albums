package dk.clausr.a1001albumsgenerator.network.retrofit.notifications

import dk.clausr.a1001albumsgenerator.network.model.ReadAllNotificationsResponse
import dk.clausr.core.model.NotificationsResponse
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface NotificationsRetrofitApi {
    @GET("api/notifications/{projectId}")
    suspend fun getNotifications(
        @Path("projectId") projectId: String,
        @Query("read") read: Boolean,
    ): NotificationsResponse

    @POST("api/notifications/{projectId}/read-all")
    suspend fun readAll(@Path("projectId") projectId: String): ReadAllNotificationsResponse
}
