package dk.clausr.core.model


data class Project(
    val name: String,
//    val currentAlbum: Album,
    val currentAlbumNotes: String,
    val updateFrequency: UpdateFrequency,
    val shareableUrl: String,
)
