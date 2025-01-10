package dk.clausr.core.model

data class Project(
    val name: String,
    val currentAlbumSlug: String,
    val currentAlbumNotes: String,
    val updateFrequency: UpdateFrequency,
    val shareableUrl: String,
)
