package dk.clausr.core.model

import kotlinx.serialization.Serializable

@Serializable
data class AlbumWidgetData(
    val coverUrl: String,
    val newAvailable: Boolean,
    val wikiLink: String,
    val streamingLinks: StreamingLinks,
)

@Serializable
data class StreamingLinks(val links: List<StreamingLink>)

@Serializable
data class StreamingLink(
    val link: String,
//    val icon: Int,
    val name: String,
)