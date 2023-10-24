package dk.clausr.core.data.model

import dk.clausr.a1001albumsgenerator.network.model.NetworkAlbum
import dk.clausr.core.model.Album

fun NetworkAlbum.asExternalModel(): Album = Album(
    artist = artist,
    artistOrigin = artistOrigin,
    images = images.asExternalModel(),
    genres = genres,
    subGenres = subGenres,
    name = name,
    slug = slug,
    releaseDate = releaseDate,
    globalReviewsUrl = globalReviewsUrl,
    wikipediaUrl = wikipediaUrl,
    spotifyId = spotifyId,
    appleMusicId = appleMusicId,
    tidalId = tidalId,
    amazonMusicId = amazonMusicId,
    youtubeMusicId = youtubeMusicId
)

