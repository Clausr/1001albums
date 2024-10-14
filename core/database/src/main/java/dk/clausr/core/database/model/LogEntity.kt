package dk.clausr.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "logs")
data class LogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val message: String,
    val level: Int,
    val tag: String,
    val timestamp: Instant = Instant.now(),
)