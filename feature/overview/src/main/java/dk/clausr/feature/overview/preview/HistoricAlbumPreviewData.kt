package dk.clausr.feature.overview.preview

import dk.clausr.core.model.Album
import dk.clausr.core.model.HistoricAlbum
import dk.clausr.core.model.Metadata
import dk.clausr.core.model.Rating
import java.time.Instant

internal fun albumPreviewData(slug: String = "paranoid") = Album(
    artist = "Black Sabbath",
    artistOrigin = "UK",
    name = "Paranoid",
    slug = slug,
    releaseDate = "1970",
    globalReviewsUrl = "https://1001albumsgenerator.com/albums/7DBES3oV6jjAmWob7kJg6P/paranoid",
    wikipediaUrl = "https://en.wikipedia.org/wiki/Paranoid_(album)",
    spotifyId = "7DBES3oV6jjAmWob7kJg6P",
    appleMusicId = "785232473",
    tidalId = 34450059,
    amazonMusicId = "B073JYN27B",
    youtubeMusicId = "OLAK5uy_l-gXxtv23EojUteRu5Zq1rKW3InI_bwsU",
    genres = emptyList(),
    subGenres = emptyList(),
    imageUrl = "https://i.scdn.co/image/ab2eae28bb2a55667ee727711aeccc7f37498414",
    qobuzId = null,
    deezerId = null,
)

internal fun historicAlbumPreviewData(
    rating: Rating = Rating.Rated(rating = 5),
    slug: String = "paranoid",
) = HistoricAlbum(
    album = albumPreviewData(slug),
    metadata = Metadata(
        rating = rating,
        review = "",
        generatedAt = Instant.now(),
        globalRating = 5.0,
        isRevealed = true,
    ),
)
