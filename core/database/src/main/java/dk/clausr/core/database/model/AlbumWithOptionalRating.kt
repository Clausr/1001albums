package dk.clausr.core.database.model

import androidx.room.Embedded

data class AlbumWithOptionalRating(
    @Embedded val album: AlbumEntity,
    @Embedded val rating: RatingEntity?, // This can be null if RatingDao is unavailable
)