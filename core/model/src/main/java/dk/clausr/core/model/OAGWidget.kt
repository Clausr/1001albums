package dk.clausr.core.model

data class OAGWidget(
    val projectName: String,
    val currentAlbumTitle: String,
    val currentAlbumArtist: String,
    val currentCoverUrl: String,
    val newAlbumAvailable: Boolean,
)
