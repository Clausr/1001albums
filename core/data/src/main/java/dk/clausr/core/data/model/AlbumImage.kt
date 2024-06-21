package dk.clausr.core.data.model

import dk.clausr.a1001albumsgenerator.network.model.NetworkAlbum
import dk.clausr.core.database.model.AlbumImageEntity
import dk.clausr.core.model.Album

fun List<NetworkAlbum.NetworkAlbumImage>.asExternalModel(): List<Album.AlbumImage> = map {
    Album.AlbumImage(
        width = it.width,
        height = it.height,
        url = it.url
    )
}

private fun NetworkAlbum.NetworkAlbumImage.toEntity(albumSlug: String): AlbumImageEntity =
    AlbumImageEntity(
        albumSlug = albumSlug,
        height = height,
        width = width,
        url = url
    )

private fun List<NetworkAlbum.NetworkAlbumImage>.toEntities(albumSlug: String): List<AlbumImageEntity> =
    map { it.toEntity(albumSlug) }

fun NetworkAlbum.toAlbumImageEntities(): List<AlbumImageEntity> =
    images.toEntities(albumSlug = slug)
