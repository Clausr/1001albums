package dk.clausr.a1001albumsgenerator.network.model

import kotlinx.serialization.Serializable

@Serializable
data class NetworkGroup(
    val id: String,
    val name: String,
    val slug: String,
)
