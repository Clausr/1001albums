package dk.clausr.core.data.model

import dk.clausr.a1001albumsgenerator.network.model.NetworkHistoricAlbum
import dk.clausr.a1001albumsgenerator.network.model.NetworkProject
import dk.clausr.core.model.Project

fun NetworkProject.asExternalModel(): Project = Project(
    name = name,
    currentAlbum = currentAlbum.asExternalModel(),
    currentAlbumNotes = currentAlbumNotes,
    history = history.map(NetworkHistoricAlbum::asExternalModel),
    updateFrequency = updateFrequency.asExternalModel(),
    shareableUrl = shareableUrl
)
