package dk.clausr.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import dk.clausr.core.database.model.AlbumImageEntity

@Dao
interface AlbumImageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(images: List<AlbumImageEntity>)
}
