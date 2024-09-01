package dk.clausr.a1001albumsgenerator.utils

import dk.clausr.a1001albumsgenerator.network.model.NotificationData
import dk.clausr.a1001albumsgenerator.network.model.NotificationResponse
import dk.clausr.a1001albumsgenerator.network.model.NotificationType
import dk.clausr.a1001albumsgenerator.network.model.NotificationsResponse
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import timber.log.Timber

object NotificationResponseSerializer : KSerializer<NotificationResponse> {
    override val descriptor: SerialDescriptor = NotificationsResponse.serializer().descriptor

    override fun serialize(
        encoder: Encoder,
        value: NotificationResponse,
    ) {
        NotificationResponse.serializer().serialize(encoder, value)
    }

    override fun deserialize(decoder: Decoder): NotificationResponse {
        require(decoder is JsonDecoder)
        val jsonObject = decoder.decodeJsonElement().jsonObject

        val type = when (val typeString = jsonObject["type"]?.jsonPrimitive?.content) {
            "groupReview" -> NotificationType.GroupReview
            "custom" -> NotificationType.Custom
            "albumsRated" -> NotificationType.AlbumsRated
            "newGroupMember" -> NotificationType.NewGroupMember
            "groupAlbumsGenerated" -> NotificationType.GroupAlbumsGenerated
            "signup" -> NotificationType.Signup
            "donationPush" -> NotificationType.DonationPush
            else -> NotificationType.Unknown
        }

        val data: NotificationData? = when (type) {
            NotificationType.GroupReview -> decoder.json.decodeFromJsonElement<NotificationData.GroupReviewData>(jsonObject["data"]!!)
            NotificationType.Custom -> decoder.json.decodeFromJsonElement<NotificationData.CustomData>(jsonObject["data"]!!)
            NotificationType.AlbumsRated -> decoder.json.decodeFromJsonElement<NotificationData.AlbumsRatedData>(jsonObject["data"]!!)
            NotificationType.GroupAlbumsGenerated ->
                decoder.json.decodeFromJsonElement<NotificationData.GroupAlbumsGeneratedData>(jsonObject["data"]!!)

            NotificationType.NewGroupMember -> decoder.json.decodeFromJsonElement<NotificationData.NewGroupMemberData>(jsonObject["data"]!!)
            NotificationType.Signup -> decoder.json.decodeFromJsonElement<NotificationData.SignupData>(jsonObject["data"]!!)
            NotificationType.DonationPush -> decoder.json.decodeFromJsonElement<NotificationData.DonationPushData>(jsonObject["data"]!!)
            NotificationType.ReviewThumpUp -> decoder.json.decodeFromJsonElement<NotificationData.ReviewThumbUpData>(jsonObject["data"]!!)
            NotificationType.Unknown -> {
                Timber.w("Unknown notification type, ${jsonObject["type"]?.jsonPrimitive?.content}")
                null
            }
        }

        return NotificationResponse(
            id = jsonObject["_id"]?.jsonPrimitive?.content ?: "",
            project = jsonObject["project"]?.jsonPrimitive?.content ?: "",
            createdAt = jsonObject["createdAt"]?.jsonPrimitive?.content ?: "",
            read = jsonObject["read"]?.jsonPrimitive?.boolean ?: false,
            type = type,
            data = data,
            version = jsonObject["__v"]?.jsonPrimitive?.int ?: 0
        )
    }
}
