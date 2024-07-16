package dk.clausr.core.model

import kotlinx.serialization.Serializable

@Serializable
data class AlbumWidgetData(
    val coverUrl: String,
    val newAvailable: Boolean,
    val wikiLink: String,
    val streamingServices: StreamingServices,
    val preferredStreamingPlatform: StreamingPlatform?,
)

@Serializable
data class StreamingServices(val services: List<StreamingService>) {
    companion object {
        fun from(album: Album): StreamingServices {
            return StreamingServices(
                listOfNotNull(
                    album.deezerId?.let {
                        StreamingService(
                            id = it,
                            platform = StreamingPlatform.Deezer,
                        )
                    },
                    album.amazonMusicId?.let {
                        StreamingService(
                            id = it,
                            platform = StreamingPlatform.AmazonMusic,
                        )
                    },
                    album.spotifyId?.let {
                        StreamingService(
                            id = it,
                            platform = StreamingPlatform.Spotify,
                        )
                    },
                    album.appleMusicId?.let {
                        StreamingService(
                            id = it,
                            platform = StreamingPlatform.AppleMusic,
                        )
                    },
                    album.tidalId?.let {
                        StreamingService(
                            id = it.toString(),
                            platform = StreamingPlatform.Tidal,
                        )
                    },
                    album.qobuzId?.let {
                        StreamingService(
                            id = it,
                            platform = StreamingPlatform.Qobuz,
                        )
                    },
                    album.youtubeMusicId?.let {
                        StreamingService(
                            id = it,
                            platform = StreamingPlatform.YouTubeMusic,
                        )
                    },
                ),
            )
        }
    }
}

enum class StreamingPlatform {
    AmazonMusic,
    AppleMusic,
    Deezer,
    Spotify,
    Tidal,
    YouTubeMusic,
    Qobuz,
}

@Serializable
data class StreamingService(private val id: String, val platform: StreamingPlatform) {
    val streamingLink: String
        get() = when (platform) {
            StreamingPlatform.Spotify -> "spotify:album:$id"
            StreamingPlatform.AppleMusic -> "https://music.apple.com/album/$id"
            StreamingPlatform.Tidal -> "https://tidal.com/browse/album/$id"
            StreamingPlatform.AmazonMusic -> "https://music.amazon.com/albums/$id"
            StreamingPlatform.YouTubeMusic -> "https://music.youtube.com/playlist?list=$id"
            StreamingPlatform.Deezer -> "https://deezer.com/album/$id"
            StreamingPlatform.Qobuz -> "https://play.qobuz.com/album/$id" // TODO Confirm
        }
}

fun Album.getStreaming(platform: StreamingPlatform): StreamingService? {
    return StreamingServices.from(this).services.firstOrNull { it.platform == platform }
}