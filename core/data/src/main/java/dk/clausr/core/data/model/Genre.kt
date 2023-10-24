package dk.clausr.core.data.model

import dk.clausr.a1001albumsgenerator.network.model.NetworkGenre
import dk.clausr.core.model.Genre

fun List<NetworkGenre>.asExternalModel(): List<Genre> = map {
    Genre(
        numberOfAlbums = it.numberOfAlbums,
        totalRating = it.totalRating,
        votes = it.votes,
        genre = it.genre,
        rating = it.rating,
        numberOfVotes = it.numberOfVotes
    )
}
