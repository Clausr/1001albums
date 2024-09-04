package dk.clausr.core.data.model.notifications

import dk.clausr.core.database.model.NotificationEntity
import dk.clausr.core.model.NotificationData
import dk.clausr.core.model.NotificationResponse
import dk.clausr.core.model.NotificationType
import dk.clausr.core.model.NotificationsResponse
import dk.clausr.core.model.notificationTypeMap
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import timber.log.Timber

fun NotificationsResponse.toEntity(): List<NotificationEntity> {
    return notifications.map { notification ->
        NotificationEntity(
            id = notification.id,
            createdAt = notification.createdAt,
            read = notification.read,
            type = notificationTypeMap.entries.first { notification.type == it.value }.key,
            data = Json.encodeToString(notification.data),
            project = notification.project,
            version = notification.version,
        )
    }
}

fun NotificationEntity.asExternalModel(): NotificationResponse {
    Timber.d("Map to external/network model: Type: $type ${this.data}")
    val notificationType = notificationTypeMap[type] ?: NotificationType.Unknown

    val json = Json {
        ignoreUnknownKeys = true
    }
    val notificationData = when (notificationType) {
        NotificationType.GroupReview -> json.decodeFromString<NotificationData.GroupReviewData>(this.data)
        NotificationType.AlbumsRated -> json.decodeFromString<NotificationData.AlbumsRatedData>(this.data)
        NotificationType.Custom -> json.decodeFromString<NotificationData.CustomData>(this.data)
        NotificationType.DonationPush -> json.decodeFromString<NotificationData.DonationPushData>(this.data)
        NotificationType.GroupAlbumsGenerated -> json.decodeFromString<NotificationData.GroupAlbumsGeneratedData>(this.data)
        NotificationType.NewGroupMember -> json.decodeFromString<NotificationData.NewGroupMemberData>(this.data)
        NotificationType.ReviewThumbUp -> json.decodeFromString<NotificationData.ReviewThumbUpData>(this.data)
        NotificationType.Signup -> json.decodeFromString<NotificationData.SignupData>(this.data)
        NotificationType.Unknown -> NotificationData.Unknown
    }

    return NotificationResponse(
        id = id,
        project = project,
        createdAt = createdAt,
        read = read,
        type = notificationType,
        data = notificationData,
        version = version,
    )
}