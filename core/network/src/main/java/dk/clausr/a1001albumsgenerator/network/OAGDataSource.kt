package dk.clausr.a1001albumsgenerator.network

import dk.clausr.a1001albumsgenerator.network.model.NetworkAlbumGroupReviews
import dk.clausr.a1001albumsgenerator.network.model.NetworkProject
import dk.clausr.core.common.model.Result
import dk.clausr.core.network.NetworkError

interface OAGDataSource {
    suspend fun getProject(projectId: String): Result<NetworkProject, NetworkError>

    suspend fun getGroupReviewsForAlbum(
        groupSlug: String,
        albumId: String,
    ): Result<NetworkAlbumGroupReviews, NetworkError>
}
