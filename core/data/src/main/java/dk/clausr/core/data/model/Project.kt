package dk.clausr.core.data.model

import dk.clausr.a1001albumsgenerator.network.model.NetworkHistoricAlbum
import dk.clausr.a1001albumsgenerator.network.model.NetworkProject
import dk.clausr.core.database.model.ProjectEntity
import dk.clausr.core.model.Project

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
    currentAlbumId = currentAlbum.slug,
    updateFrequency = updateFrequency.asExternalModel()
)
