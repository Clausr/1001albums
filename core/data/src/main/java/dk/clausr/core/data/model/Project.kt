package dk.clausr.core.data.model

import dk.clausr.a1001albumsgenerator.network.model.NetworkProject
import dk.clausr.core.database.model.ProjectEntity
import dk.clausr.core.model.Project

fun NetworkProject.asExternalModel(): Project = Project(
    name = name,
    currentAlbumSlug = currentAlbum.slug,
    currentAlbumNotes = currentAlbumNotes,
    updateFrequency = frequency.asExternalModel(),
    shareableUrl = shareableUrl,
    group = getGroupAsExternal(),
)

fun NetworkProject.toEntity(): ProjectEntity = ProjectEntity(
    name = name,
    shareableUrl = shareableUrl,
    currentAlbumNotes = currentAlbumNotes,
    currentAlbumSlug = currentAlbum.slug,
    updateFrequency = frequency.asExternalModel(),
    groupSlug = group?.slug,
    isGroupPaused = group?.paused == true,
)

fun ProjectEntity.asExternalModel(): Project = Project(
    name = name,
    currentAlbumSlug = currentAlbumSlug,
    currentAlbumNotes = currentAlbumNotes,
    updateFrequency = updateFrequency,
    shareableUrl = shareableUrl,
    group = getGroup(),
)

fun NetworkProject.getGroupAsExternal(): Project.Group? = group?.let {
    Project.Group(
        slug = it.slug,
        updateFrequency = it.updateFrequency.asExternalModel(),
        paused = it.paused,
    )
}

fun ProjectEntity.getGroup(): Project.Group? = groupSlug?.let { slug ->
    Project.Group(
        slug = slug,
        updateFrequency = updateFrequency,
        paused = isGroupPaused,
    )
}
