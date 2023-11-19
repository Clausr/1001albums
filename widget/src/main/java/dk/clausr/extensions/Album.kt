package dk.clausr.extensions

import dk.clausr.core.model.Album

fun Album.getCoverUrl(): String = images.maxBy { it.height }.url
