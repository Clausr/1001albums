package dk.clausr.a1001albumsgenerator.network.model

import dk.clausr.a1001albumsgenerator.utils.NotificationResponseSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NotificationsResponse(
    val success: Boolean,
    val notifications: List<NotificationResponse>,
)

@Serializable(with = NotificationResponseSerializer::class)
data class NotificationResponse(
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
    data object GroupReview : NotificationType()

    @Serializable
    data object Custom : NotificationType()

    @Serializable
    data object AlbumsRated : NotificationType()

    @Serializable
    data object NewGroupMember : NotificationType()

    @Serializable
    data object GroupAlbumsGenerated : NotificationType()

    @Serializable
    data object Signup : NotificationType()

    @Serializable
    data object DonationPush : NotificationType()

    @Serializable
    data object ReviewThumpUp : NotificationType()

    data object Unknown : NotificationType()
    // Add other notification types here as needed
}

@Serializable
sealed class NotificationData {
    @Serializable
    @SerialName("groupReviewData")
    data class GroupReviewData(
        val albumName: String,
        val albumId: String,
        val rating: Int,
        val groupSlug: String,
        val projectName: String,
        val isUserAlbum: Boolean?,
    ) : NotificationData()

    @Serializable
    @SerialName("custom")
    data class CustomData(
        val heading: String,
        val body: String,
    ) : NotificationData()

    @Serializable
    @SerialName("albumsRated")
    data class AlbumsRatedData(
        val numberOfAlbums: Int,
    ) : NotificationData()

    @Serializable
    @SerialName("newGroupMemberData")
    data class NewGroupMemberData(
        val groupSlug: String,
    ) : NotificationData()

    @Serializable
    @SerialName("groupAlbumsGeneratedData")
    data class GroupAlbumsGeneratedData(
        val numberOfAlbums: Int,
        val groupId: String,
        val groupSlug: String,
    ) : NotificationData()

    @Serializable
    @SerialName("signupData")
    data class SignupData(
        val projectName: String,
    ) : NotificationData()

    @Serializable
    @SerialName("donationPushData")
    data class DonationPushData(
        val projectName: String,
    ) : NotificationData()

    @Serializable
    @SerialName("reviewThumpUpData")
    data class ReviewThumbUpData(
        val reviewId: String,
        val albumName: String,
        val albumSlug: String,
        val thumbsUp: Int,
        val isUserAlbum: Boolean?,
    ) : NotificationData()
}