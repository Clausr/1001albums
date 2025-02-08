package dk.clausr.a1001albumsgenerator.network.model

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class NetworkAlbumGroupReview(
    val albumName: String,
    val albumArtist: String,
    val projectName: String,
    val rating: String? = null,
    val review: String? = null,
    @Contextual val generatedAt: Instant,
)