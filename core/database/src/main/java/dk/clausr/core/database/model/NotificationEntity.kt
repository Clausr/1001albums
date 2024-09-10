package dk.clausr.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey
    val id: String,
    val createdAt: Instant,
    val read: Boolean,
    val project: String,
    val version: Int,
    val type: String, // Store type as String
    val data: String, // Store data as Json
)
