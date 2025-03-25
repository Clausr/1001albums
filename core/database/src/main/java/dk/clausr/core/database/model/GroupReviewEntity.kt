package dk.clausr.core.database.model

import androidx.room.Entity

@Entity(
    tableName = "group_reviews",
    primaryKeys = ["albumId", "author"],
)
data class GroupReviewEntity(
    val author: String,
    val albumId: String,
    val rating: String?,
    val review: String?,
)
