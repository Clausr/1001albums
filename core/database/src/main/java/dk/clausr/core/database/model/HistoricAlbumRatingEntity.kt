package dk.clausr.core.database.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(
    tableName = "rating",
    foreignKeys = [
        ForeignKey(
            AlbumEntity::class,
            parentColumns = ["slug"],
            childColumns = ["albumSlug"],
            onDelete = ForeignKey.CASCADE,
        )
    ]
)
data class HistoricAlbumRatingEntity(
    @PrimaryKey
    val albumSlug: String,
    val rating: String,
    val review: String,
    val generatedAt: Instant,
    val globalRating: Double,
)
