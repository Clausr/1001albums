package dk.clausr.a1001albumsgenerator.network.model

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class NetworkProject(
    val name: String,
    val currentAlbum: NetworkAlbum,
    val currentAlbumNotes: String,
    val history: List<NetworkHistoricAlbum>,
    internal val updateFrequency: NetworkUpdateFrequency,
    val shareableUrl: String,
    val group: NetworkGroup? = null,
) {
    // Groups updateFrequency overrides the individual
    val frequency: NetworkUpdateFrequency
        get() = group?.updateFrequency ?: updateFrequency

    @Serializable
    data class NetworkGroup(
        val slug: String,
        val updateFrequency: NetworkUpdateFrequency,
        val paused: Boolean = false,
    )
}

@Serializable
data class NetworkHistoricAlbum(
    val album: NetworkAlbum,
    val rating: String? = null,
    val review: String = "",
    @Contextual val generatedAt: Instant,
    val globalRating: Double,
    @SerialName("revealedAlbum")
    val isRevealed: Boolean = false,
)

@Serializable
enum class NetworkUpdateFrequency {
    @SerialName("dailyWithWeekends")
    DailyWithWeekends,

    @SerialName("daily")
    DailyWithoutWeekends,
}
