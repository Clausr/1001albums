package dk.clausr.core.model

data class GroupReview(
    val author: String,
    val rating: Rating?,
    val review: String?,
)
