package dk.clausr.core.model


data class Project(
    val name: String,
    val currentAlbum: Album,
    val currentAlbumNotes: String,
    val history: List<HistoricAlbum>,
    val updateFrequency: UpdateFrequency,
    val shareableUrl: String,
)
