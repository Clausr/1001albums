package dk.clausr.core.data.model

import dk.clausr.a1001albumsgenerator.network.model.NetworkAlbumGroupReviews
import dk.clausr.core.database.model.GroupReviewEntity
import dk.clausr.core.model.GroupReview

fun NetworkAlbumGroupReviews.toEntity(albumId: String): List<GroupReviewEntity> = reviews.map { review ->
    GroupReviewEntity(
        author = review.projectName,
        albumId = albumId,
        rating = review.rating,
        review = review.review,
    )
}

fun GroupReviewEntity.asExternalModel(): GroupReview = GroupReview(
    author = author,
    rating = rating.mapToRating(),
    review = review,
)

fun List<GroupReviewEntity>.asExternalModel(): List<GroupReview> = map(GroupReviewEntity::asExternalModel)
