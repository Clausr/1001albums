package dk.clausr.core.data.model

import dk.clausr.a1001albumsgenerator.network.model.NetworkAlbum
import dk.clausr.core.database.model.AlbumEntity
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

fun NetworkAlbum.toEntity(): AlbumEntity = AlbumEntity(
    slug = slug,
    artist = artist,
    artistOrigin = artistOrigin,
    name = name,
    releaseDate = releaseDate,
    globalReviewsUrl = globalReviewsUrl,
    wikipediaUrl = wikipediaUrl,
    spotifyId = spotifyId,
    appleMusicId = appleMusicId,
    tidalId = tidalId,
    amazonMusicId = amazonMusicId,
    youtubeMusicId = youtubeMusicId
)


fun AlbumEntity.asExternalModel(): Album = Album(
    artist = artist,
    artistOrigin = artist,
    images = emptyList(),
    genres = emptyList(),
    subGenres = emptyList(),
    name = name,
    slug = slug,
    releaseDate = releaseDate,
    globalReviewsUrl = globalReviewsUrl,
    wikipediaUrl = wikipediaUrl,
    spotifyId = spotifyId,
    appleMusicId = appleMusicId,
    tidalId = tidalId,
    amazonMusicId = amazonMusicId,
    youtubeMusicId = youtubeMusicId,
)
