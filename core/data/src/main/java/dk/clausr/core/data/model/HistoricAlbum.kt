package dk.clausr.core.data.model

import dk.clausr.a1001albumsgenerator.network.model.NetworkHistoricAlbum
import dk.clausr.core.database.model.AlbumWithOptionalRating
import dk.clausr.core.database.model.RatingEntity
import dk.clausr.core.database.model.RatingWithAlbum
import dk.clausr.core.model.HistoricAlbum
import dk.clausr.core.model.Metadata

fun NetworkHistoricAlbum.asExternalModel(): HistoricAlbum {
    return HistoricAlbum(
        album = album.asExternalModel(),
        metadata = Metadata(
            rating = rating.mapToRating(),
            review = review,
            generatedAt = generatedAt,
            globalRating = globalRating,
            isRevealed = isRevealed,
        ),
    )
}

fun NetworkHistoricAlbum.toRatingEntity(): RatingEntity = RatingEntity(
    albumSlug = album.slug,
    rating = rating,
    review = review,
    generatedAt = generatedAt,
    globalRating = globalRating,
    isRevealed = isRevealed,
)

fun RatingWithAlbum.mapToHistoricAlbum(): HistoricAlbum = HistoricAlbum(
    album = album.asExternalModel(),
    metadata = Metadata(
        rating = rating.rating.mapToRating(),
        review = rating.review,
        generatedAt = rating.generatedAt,
        globalRating = rating.globalRating,
        isRevealed = rating.isRevealed,
    ),
)

fun AlbumWithOptionalRating.mapToHistoricAlbum(): HistoricAlbum = HistoricAlbum(
    album = album.asExternalModel(),
    metadata = rating?.let {
        Metadata(
            rating = it.rating.mapToRating(),
            review = it.review,
            generatedAt = it.generatedAt,
            globalRating = it.globalRating,
            isRevealed = it.isRevealed,
        )
    },
)
