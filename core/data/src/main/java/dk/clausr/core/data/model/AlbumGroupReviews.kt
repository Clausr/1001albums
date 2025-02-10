package dk.clausr.core.data.model

import dk.clausr.a1001albumsgenerator.network.model.NetworkAlbumGroupReviews
import dk.clausr.core.model.AlbumGroupReviews

fun NetworkAlbumGroupReviews.asExternalModel(): AlbumGroupReviews = AlbumGroupReviews(
    albumName = albumName,
    albumArtist = albumArtist,
    reviews = reviews.map(NetworkAlbumGroupReviews.NetworkReview::asExternalModel),
)

private fun NetworkAlbumGroupReviews.NetworkReview.asExternalModel(): AlbumGroupReviews.GroupReview = AlbumGroupReviews.GroupReview(
    projectName = projectName,
    rating = rating.mapToRating(),
    review = review,
)
