package dk.clausr.core.data.model

import dk.clausr.a1001albumsgenerator.network.model.NetworkProject
import dk.clausr.core.database.model.ProjectEntity
import dk.clausr.core.model.Project

fun NetworkProject.asExternalModel(): Project = Project(
    name = name,
    currentAlbumSlug = currentAlbum.slug,
    currentAlbumNotes = currentAlbumNotes,
    updateFrequency = updateFrequency.asExternalModel(),
    shareableUrl = shareableUrl,
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
    currentAlbumSlug = currentAlbumSlug,
    currentAlbumNotes = currentAlbumNotes,
    updateFrequency = updateFrequency,
    shareableUrl = shareableUrl,
)
