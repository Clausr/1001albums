package dk.clausr.core.data.model.notifications

import dk.clausr.core.database.model.NotificationEntity
import dk.clausr.core.model.Notification
import dk.clausr.core.model.NotificationData
import dk.clausr.core.model.NotificationType
import dk.clausr.core.model.notificationTypeMap
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

fun List<Notification>.toEntities(): List<NotificationEntity> = map(Notification::toEntity)

fun Notification.toEntity(): NotificationEntity {
    val notification = this
    return NotificationEntity(
        id = notification.id,
        createdAt = notification.createdAt,
        read = notification.read,
        type = notificationTypeMap.entries.first { notification.type == it.value }.key,
        data = Json.encodeToString(notification.data),
        project = notification.project,
        version = notification.version,
    )
}

fun NotificationEntity.asExternalModel(): Notification {
    val notificationType = notificationTypeMap[type] ?: NotificationType.Unknown

    val json = Json {
        ignoreUnknownKeys = true
    }
    val notificationData = when (notificationType) {
        NotificationType.GroupReview -> json.decodeFromString<NotificationData.GroupReviewData>(this.data)
        NotificationType.AlbumsRated -> json.decodeFromString<NotificationData.AlbumsRatedData>(this.data)
        NotificationType.GroupAlbumsGenerated -> json.decodeFromString<NotificationData.GroupAlbumsGeneratedData>(this.data)
        NotificationType.NewGroupMember -> json.decodeFromString<NotificationData.NewGroupMemberData>(this.data)
        NotificationType.ReviewThumbUp -> json.decodeFromString<NotificationData.ReviewThumbUpData>(this.data)
        NotificationType.Unknown -> null
    }

    return Notification(
        id = id,
        project = project,
        createdAt = createdAt,
        read = read,
        type = notificationType,
        data = notificationData,
        version = version,
    )
}