package dk.clausr.core.data.model

import dk.clausr.core.model.Rating

fun String?.mapToRating(): Rating = when (this) {
    "did-not-listen" -> Rating.DidNotListen
    null -> Rating.Unrated
    else -> Rating.Rated(this.toInt())
}