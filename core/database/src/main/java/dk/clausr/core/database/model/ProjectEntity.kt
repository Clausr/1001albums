package dk.clausr.core.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import dk.clausr.core.model.UpdateFrequency

@Entity(tableName = "project")
data class ProjectEntity(
    @PrimaryKey
    val name: String,
    val currentAlbumSlug: String,
    val currentAlbumNotes: String,
    val updateFrequency: UpdateFrequency,
    val shareableUrl: String,
    val groupSlug: String?,
    // Group UUID from the /groups/{slug} endpoint; used as the history-page deep-link row suffix.
    val groupId: String? = null,
    @ColumnInfo(defaultValue = "0")
    val isGroupPaused: Boolean = false,
)
