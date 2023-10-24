package dk.clausr.core.data.model

import dk.clausr.a1001albumsgenerator.network.model.NetworkAlbum
import dk.clausr.core.model.Album

fun List<NetworkAlbum.NetworkAlbumImage>.asExternalModel(): List<Album.AlbumImage> = map {
    Album.AlbumImage(
        width = it.width,
        height = it.height,
        url = it.url
    )
}
