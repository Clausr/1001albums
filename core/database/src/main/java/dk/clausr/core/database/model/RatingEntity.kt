package dk.clausr.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "ratings")
data class RatingEntity(
    @PrimaryKey
    val albumSlug: String,
    val rating: String?,
    val review: String,
    val generatedAt: Instant,
    val globalRating: Double,
)
