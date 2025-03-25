package dk.clausr.core.model

data class Project(
    val name: String,
    val currentAlbumSlug: String,
    val currentAlbumNotes: String,
    val updateFrequency: UpdateFrequency,
    val shareableUrl: String,
    val group: Group?,
) {
    data class Group(
        val slug: String,
        val updateFrequency: UpdateFrequency,
        val paused: Boolean,
    )
}
