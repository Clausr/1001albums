package dk.clausr.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import dk.clausr.core.model.Album
import dk.clausr.core.model.Project
import dk.clausr.core.model.UpdateFrequency

@Entity(tableName = "project")
data class ProjectEntity(
    @PrimaryKey
    val name: String,
    val currentAlbumId: String,
    val currentAlbumNotes: String,
    val updateFrequency: UpdateFrequency,
    val shareableUrl: String,
)

fun ProjectEntity.asExternalModel(): Project = Project(
    name = name,
    currentAlbum = Album(
        artist = "",
        artistOrigin = "",
        images = emptyList(),
        genres = emptyList(),
        subGenres = emptyList(),
        name = "",
        slug = "",
        releaseDate = "",
        globalReviewsUrl = "",
        wikipediaUrl = "",
        spotifyId = "",
        appleMusicId = "",
        tidalId = 0,
        amazonMusicId = "",
        youtubeMusicId = "",
    ),
    currentAlbumNotes = currentAlbumNotes,
    history = emptyList(),
    updateFrequency = UpdateFrequency.DailyWithWeekends,
    shareableUrl = shareableUrl,
)
