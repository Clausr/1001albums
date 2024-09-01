package dk.clausr.a1001albumsgenerator.network.retrofit

import dk.clausr.a1001albumsgenerator.network.model.NetworkProject
import dk.clausr.a1001albumsgenerator.network.model.NotificationsResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface OAGRetrofitApi {
    @GET("api/v1/projects/{projectId}")
    suspend fun getProject(@Path("projectId") projectId: String): NetworkProject

    @GET("api/notifications/{projectId}")
    suspend fun getNotifications(
        @Path("projectId") projectId: String,
        @Query("read") read: Boolean,
    ): NotificationsResponse
}
