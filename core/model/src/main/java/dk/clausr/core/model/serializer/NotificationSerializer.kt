package dk.clausr.core.model.serializer

import dk.clausr.core.model.Notification
import dk.clausr.core.model.NotificationData
import dk.clausr.core.model.NotificationType
import dk.clausr.core.model.NotificationsResponse
import dk.clausr.core.model.notificationTypeMap
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
import java.time.Instant

object NotificationSerializer : KSerializer<Notification> {
    override val descriptor: SerialDescriptor = NotificationsResponse.serializer().descriptor

    override fun serialize(
        encoder: Encoder,
        value: Notification,
    ) {
        Notification.serializer().serialize(encoder, value)
    }

    override fun deserialize(decoder: Decoder): Notification {
        require(decoder is JsonDecoder)
        val jsonObject = decoder.decodeJsonElement().jsonObject

        val type = notificationTypeMap[jsonObject["type"]?.jsonPrimitive?.content] ?: NotificationType.Unknown

        val data = when (type) {
            NotificationType.GroupReview -> decoder.json.decodeFromJsonElement<NotificationData.GroupReviewData>(jsonObject["data"]!!)
            NotificationType.AlbumsRated -> decoder.json.decodeFromJsonElement<NotificationData.AlbumsRatedData>(jsonObject["data"]!!)
            NotificationType.GroupAlbumsGenerated ->
                decoder.json.decodeFromJsonElement<NotificationData.GroupAlbumsGeneratedData>(jsonObject["data"]!!)

            NotificationType.NewGroupMember -> decoder.json.decodeFromJsonElement<NotificationData.NewGroupMemberData>(jsonObject["data"]!!)
            NotificationType.ReviewThumbUp -> decoder.json.decodeFromJsonElement<NotificationData.ReviewThumbUpData>(jsonObject["data"]!!)
            NotificationType.Unknown -> null
        }

        return Notification(
            id = jsonObject["_id"]?.jsonPrimitive?.content ?: "",
            project = jsonObject["project"]?.jsonPrimitive?.content ?: "",
            createdAt = jsonObject["createdAt"]?.jsonPrimitive?.content?.let(Instant::parse) ?: Instant.now(),
            read = jsonObject["read"]?.jsonPrimitive?.boolean ?: false,
            type = type,
            data = data,
            version = jsonObject["__v"]?.jsonPrimitive?.int ?: 0,
        )
    }
}
