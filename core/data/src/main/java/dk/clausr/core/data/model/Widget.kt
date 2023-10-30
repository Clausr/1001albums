package dk.clausr.core.data.model

import dk.clausr.core.database.model.WidgetEntity
import dk.clausr.core.model.OAGWidget

fun WidgetEntity.asExternalModel(): OAGWidget = OAGWidget(projectName, currentAlbumTitle, currentAlbumArtist, currentCoverUrl)
