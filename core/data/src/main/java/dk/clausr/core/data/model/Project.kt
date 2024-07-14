package dk.clausr.core.data.model

import dk.clausr.a1001albumsgenerator.network.model.NetworkProject
import dk.clausr.core.database.model.ProjectEntity
import dk.clausr.core.model.HistoricAlbum
import dk.clausr.core.model.Project
import dk.clausr.core.model.UpdateFrequency

fun NetworkProject.asExternalModel(): Project = Project(
    name = name,
    currentAlbumSlug = currentAlbum.slug,
    currentAlbumNotes = currentAlbumNotes,
    updateFrequency = updateFrequency.asExternalModel(),
    shareableUrl = shareableUrl,
    historicAlbums = history.map { it.asExternalModel() },
)

fun NetworkProject.toEntity(): ProjectEntity = ProjectEntity(
    name = name,
    shareableUrl = shareableUrl,
    currentAlbumNotes = currentAlbumNotes,
    currentAlbumSlug = currentAlbum.slug,
    updateFrequency = updateFrequency.asExternalModel(),
)

fun ProjectEntity.asExternalModel(history: List<HistoricAlbum>): Project = Project(
    name = name,
    currentAlbumSlug = currentAlbumSlug,
    currentAlbumNotes = currentAlbumNotes,
    updateFrequency = UpdateFrequency.DailyWithWeekends,
    shareableUrl = shareableUrl,
    historicAlbums = history,
)
