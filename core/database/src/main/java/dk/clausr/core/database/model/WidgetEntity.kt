package dk.clausr.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("widget")
data class WidgetEntity(
    @PrimaryKey
    val projectName: String,
    val currentAlbumTitle: String,
    val currentAlbumArtist: String,
    val currentCoverUrl: String,
    val newAlbumAvailable: Boolean,
//    val albumDate: LocalDate,
)
