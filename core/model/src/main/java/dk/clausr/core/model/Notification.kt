package dk.clausr.core.model

import dk.clausr.core.model.NotificationType.AlbumsRated
import dk.clausr.core.model.NotificationType.GroupAlbumsGenerated
import dk.clausr.core.model.NotificationType.GroupReview
import dk.clausr.core.model.NotificationType.NewGroupMember
import dk.clausr.core.model.NotificationType.ReviewThumbUp
import dk.clausr.core.model.serializer.NotificationSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NotificationsResponse(
    val success: Boolean,
    val notifications: List<Notification>,
)

@Serializable(with = NotificationSerializer::class)
data class Notification(
    @SerialName("_id") val id: String,
    val project: String,
    val createdAt: String,
    val read: Boolean,
    val type: NotificationType,
    val data: NotificationData?,
    @SerialName("__v") val version: Int,
)

sealed class NotificationType {
    @Serializable
    @SerialName("groupReview")
    data object GroupReview : NotificationType()

    @Serializable
    @SerialName("albumsRated")
    data object AlbumsRated : NotificationType()

    @Serializable
    @SerialName("newGroupMember")
    data object NewGroupMember : NotificationType()

    @Serializable
    @SerialName("groupAlbumsGenerated")
    data object GroupAlbumsGenerated : NotificationType()

    @Serializable
    @SerialName("reviewThumpUp")
    data object ReviewThumbUp : NotificationType()

    @Serializable
    @SerialName("unknown")
    data object Unknown : NotificationType()
}

val notificationTypeMap: Map<String, NotificationType> = mapOf(
    "groupReview" to GroupReview,
    "albumsRated" to AlbumsRated,
    "newGroupMember" to NewGroupMember,
    "groupAlbumsGenerated" to GroupAlbumsGenerated,
    "reviewThumpUp" to ReviewThumbUp,
)

@Serializable
sealed class NotificationData {
    @Serializable
    @SerialName("groupReview")
    data class GroupReviewData(
        val albumName: String,
        val albumId: String,
        val rating: Int,
        val groupSlug: String,
        val projectName: String,
        val isUserAlbum: Boolean? = false,
    ) : NotificationData()

    @Serializable
    @SerialName("albumsRated")
    data class AlbumsRatedData(
        val numberOfAlbums: Int,
    ) : NotificationData()

    @Serializable
    @SerialName("newGroupMember")
    data class NewGroupMemberData(
        val groupSlug: String,
    ) : NotificationData()

    @Serializable
    @SerialName("groupAlbumsGenerated")
    data class GroupAlbumsGeneratedData(
        val numberOfAlbums: Int,
        val groupId: String,
        val groupSlug: String,
    ) : NotificationData()

    @Serializable
    @SerialName("reviewThumpUp")
    data class ReviewThumbUpData(
        val reviewId: String,
        val albumName: String,
        val albumSlug: String,
        val thumbsUp: Int,
        val isUserAlbum: Boolean?,
    ) : NotificationData()
}
