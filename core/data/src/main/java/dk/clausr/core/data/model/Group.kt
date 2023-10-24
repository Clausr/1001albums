package dk.clausr.core.data.model

import dk.clausr.a1001albumsgenerator.network.model.NetworkAlbum
import dk.clausr.a1001albumsgenerator.network.model.NetworkFilteredSelection
import dk.clausr.a1001albumsgenerator.network.model.NetworkGroup
import dk.clausr.core.model.FilteredSelection
import dk.clausr.core.model.Group

fun NetworkGroup.asExternalModel(): Group = Group(
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

fun NetworkFilteredSelection.asExternalModel(): FilteredSelection = FilteredSelection(selections = selections, genres = genres)

