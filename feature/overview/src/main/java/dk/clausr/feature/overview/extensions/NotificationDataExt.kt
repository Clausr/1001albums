package dk.clausr.feature.overview.extensions

internal val String.sluggify: String
    get() = lowercase()
        .replace("[^a-zA-Z0-9 ]+".toRegex(), "")
        .replace(" ", "-")
