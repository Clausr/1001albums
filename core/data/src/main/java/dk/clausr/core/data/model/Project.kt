package dk.clausr.core.data.model

import dk.clausr.a1001albumsgenerator.network.model.NetworkHistoricAlbum
import dk.clausr.a1001albumsgenerator.network.model.NetworkProject
import dk.clausr.core.database.model.ProjectEntity
import dk.clausr.core.model.Album
import dk.clausr.core.model.Project
import dk.clausr.core.model.UpdateFrequency

fun NetworkProject.asExternalModel(): Project = Project(
    name = name,
    currentAlbum = currentAlbum.asExternalModel(),
    currentAlbumNotes = currentAlbumNotes,
    history = history.map(NetworkHistoricAlbum::asExternalModel),
    updateFrequency = updateFrequency.asExternalModel(),
    shareableUrl = shareableUrl
)

fun NetworkProject.toEntity(): ProjectEntity = ProjectEntity(
    name = name,
    shareableUrl = shareableUrl,
    currentAlbumNotes = currentAlbumNotes,
    currentAlbumSlug = currentAlbum.slug,
    updateFrequency = updateFrequency.asExternalModel(),
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
