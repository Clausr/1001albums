package dk.clausr.a1001albumsgenerator.network.retrofit

import dk.clausr.a1001albumsgenerator.network.model.NetworkGroup
import dk.clausr.a1001albumsgenerator.network.model.NetworkProject
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface OAGRetrofitApi {
    @GET("groups/{groupId}")
    suspend fun getGroups(@Path("groupId") groupId: String): Response<NetworkGroup>

    @GET("projects/{projectId}")
    suspend fun getProject(@Path("projectId") projectId: String): Response<NetworkProject>
}
