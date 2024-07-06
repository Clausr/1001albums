package dk.clausr.core.database.model

import androidx.room.Entity
import java.time.Instant

@Entity(tableName = "ratings", primaryKeys = ["albumSlug", "generatedAt"])
data class RatingEntity(
    val albumSlug: String,
    val rating: String?,
    val review: String,
    val generatedAt: Instant,
    val globalRating: Double,
)
