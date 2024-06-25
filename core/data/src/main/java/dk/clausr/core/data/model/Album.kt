package dk.clausr.core.data.model

import dk.clausr.a1001albumsgenerator.network.model.NetworkAlbum
import dk.clausr.core.database.model.AlbumEntity
import dk.clausr.core.model.Album

fun NetworkAlbum.asExternalModel(): Album = Album(
    artist = artist,
    artistOrigin = artistOrigin,
    imageUrl = images.maxBy { it.width }.url,
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
    youtubeMusicId = youtubeMusicId,
    deezerId = deezerId,
    qobuzId = qobuzId,
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
    youtubeMusicId = youtubeMusicId,
    imageUrl = images.maxBy { it.width }.url,
    deezerId = deezerId,
    qobuzId = qobuzId,
)


fun AlbumEntity.asExternalModel(): Album = Album(
    artist = artist,
    artistOrigin = artist,
    imageUrl = imageUrl,
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
    deezerId = deezerId,
    qobuzId = qobuzId,
)
