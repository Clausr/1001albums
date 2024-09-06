package dk.clausr.a1001albumsgenerator.network.model

import kotlinx.serialization.Serializable

@Serializable
data class ReadAllNotificationsResponse(
    val success: Boolean,
)
