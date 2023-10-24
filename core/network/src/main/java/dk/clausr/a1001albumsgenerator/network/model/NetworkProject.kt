package dk.clausr.a1001albumsgenerator.network.model

import kotlinx.serialization.Serializable

@Serializable
data class NetworkProject(
    val name: String,
    val currentAlbum: NetworkAlbum,
    val currentAlbumNotes: String,
    val history: List<NetworkHistoricAlbum>,
    val updateFrequency: NetworkUpdateFrequency,
    val shareableUrl: String,
)

@Serializable
data class NetworkHistoricAlbum(
    val album: NetworkAlbum,
    val rating: String,
    val review: String,
    val generatedAt: String, // TODO LocalDateTime
    val globalRating: Double,
)

