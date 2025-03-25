package dk.clausr.a1001albumsgenerator.network.model

import kotlinx.serialization.Serializable

@Serializable
data class NetworkAlbumGroupReviews(
    val albumName: String,
    val albumArtist: String,
    val reviews: List<NetworkReview>,
) {
    @Serializable
    data class NetworkReview(
        val projectName: String,
        val rating: String? = null,
        val review: String? = null,
    )
}
