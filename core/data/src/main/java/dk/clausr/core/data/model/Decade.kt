package dk.clausr.core.data.model

import dk.clausr.a1001albumsgenerator.network.model.NetworkDecade
import dk.clausr.core.model.Decade


fun List<NetworkDecade>.asExternalModel(): List<Decade> =
    map { Decade(totalRating = it.totalRating, votes = it.votes, numberOfAlbums = it.numberOfAlbums, decade = it.decade, rating = it.rating) }
