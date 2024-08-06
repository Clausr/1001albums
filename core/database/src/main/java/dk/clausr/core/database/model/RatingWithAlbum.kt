package dk.clausr.core.database.model

import androidx.room.Embedded
import androidx.room.Relation

data class RatingWithAlbum(
    @Embedded val rating: RatingEntity,
    @Relation(
        parentColumn = "albumSlug",
        entityColumn = "slug",
    )
    val album: AlbumEntity,
)
