package dk.clausr.core.data.model

import dk.clausr.a1001albumsgenerator.network.model.NetworkAlbumGroupReviews
import dk.clausr.core.model.GroupReview

fun NetworkAlbumGroupReviews.asExternalModel(): List<GroupReview> = reviews.map(NetworkAlbumGroupReviews.NetworkReview::asExternalModel)

fun NetworkAlbumGroupReviews.NetworkReview.asExternalModel(): GroupReview = GroupReview(
    author = projectName,
    rating = rating.mapToRating(),
    review = review,
)
