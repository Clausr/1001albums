package dk.clausr.core.data.model

import dk.clausr.a1001albumsgenerator.network.model.NetworkAlbum
import dk.clausr.core.database.model.AlbumEntity
import dk.clausr.core.model.Album

fun NetworkAlbum.asExternalModel(): Album = Album(
    id = id,
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

fun NetworkAlbum.toEntity(isUserAlbum: Boolean = false): AlbumEntity = AlbumEntity(
    id = id,
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
    isUserAlbum = isUserAlbum,
)

fun AlbumEntity.asExternalModel(): Album = Album(
    id = id,
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
    isUserAlbum = isUserAlbum,
)
