package dk.clausr.core.database.model

import androidx.room.Entity

@Entity(
    tableName = "album_images",
    primaryKeys = ["albumSlug", "height"],
)
data class AlbumImageEntity(
    val albumSlug: String,
    val height: Int,
    val width: Int,
    val url: String,
)
