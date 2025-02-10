package dk.clausr.core.model

data class AlbumGroupReviews(
    val albumName: String,
    val albumArtist: String,
    val reviews: List<GroupReview>,
) {
    data class GroupReview(
        val projectName: String,
        val rating: Rating?,
        val review: String?,
    )
}


