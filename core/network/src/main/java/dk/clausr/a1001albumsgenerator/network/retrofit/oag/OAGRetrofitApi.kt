package dk.clausr.a1001albumsgenerator.network.retrofit.oag

import dk.clausr.a1001albumsgenerator.network.model.NetworkAlbumGroupReviews
import dk.clausr.a1001albumsgenerator.network.model.NetworkProject
import retrofit2.http.GET
import retrofit2.http.Path

interface OAGRetrofitApi {
    @GET("api/v1/projects/{projectId}")
    suspend fun getProject(@Path("projectId") projectId: String): NetworkProject

    @GET("/api/v1/groups/{groupSlug}/albums/{albumUuid}")
    suspend fun getGroupReviewsForAlbum(
        @Path("groupSlug") groupSlug: String,
        @Path("albumUuid") albumId: String,
    ): NetworkAlbumGroupReviews
}
