package dk.clausr.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import dk.clausr.core.database.model.NotificationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {
    @Upsert
    suspend fun insertNotification(notification: NotificationEntity)

    @Upsert
    suspend fun insertNotifications(notifications: List<NotificationEntity>)

    @Query("SELECT * FROM notifications")
    fun getNotifications(): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications WHERE read = 0")
    fun getUnreadNotifications(): Flow<List<NotificationEntity>>

    @Query("UPDATE notifications SET read = 1 WHERE read = 0")
    suspend fun readNotifications()

    @Query("SELECT count(id) FROM notifications")
    suspend fun getNotificationCount(): Int
}
