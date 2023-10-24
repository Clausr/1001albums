package dk.clausr.core.data.model

import dk.clausr.a1001albumsgenerator.network.model.NetworkAlbum
import dk.clausr.a1001albumsgenerator.network.model.NetworkFilteredSelection
import dk.clausr.a1001albumsgenerator.network.model.NetworkGroupResponse
import dk.clausr.core.model.Album
import dk.clausr.core.model.FilteredSelection
import dk.clausr.core.model.Group

fun NetworkGroupResponse.asExternalModel(): Group = Group(
    name = name,
    slug = slug,
    updateFrequency = updateFrequency.asExternalModel(),
    filteredSelection = filteredSelection.asExternalModel(),
    currentAlbum = currentAlbum.asExternalModel(),
    latestAlbum = latestAlbum.asExternalModel(),
    highestRatedAlbums = highestRatedAlbums.map(NetworkAlbum::asExternalModel),
    lowestRatedAlbums = lowestRatedAlbums.map(NetworkAlbum::asExternalModel),
    favoriteGenres = favoriteGenres.asExternalModel(),
    worstGenres = worstGenres.asExternalModel(),
    ratingByDecade = ratingByDecade.asExternalModel(),
    numberOfGeneratedAlbums = numberOfGeneratedAlbums,
    totalVotes = totalVotes
)

fun NetworkGroupResponse.NetworkUpdateFrequency.asExternalModel(): Group.UpdateFrequency =
    Group.UpdateFrequency.values().first { it.name.equals(name, ignoreCase = true) }

fun NetworkFilteredSelection.asExternalModel(): FilteredSelection = FilteredSelection(selections = selections, genres = genres)
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


