package dk.clausr.core.data.model

import dk.clausr.a1001albumsgenerator.network.model.NetworkHistoricAlbum
import dk.clausr.core.database.model.RatingEntity

fun NetworkHistoricAlbum.toRatingEntity(): RatingEntity = RatingEntity(
    albumSlug = album.slug,
    rating = rating,
    review = review,
    generatedAt = generatedAt,
    globalRating = globalRating,
)