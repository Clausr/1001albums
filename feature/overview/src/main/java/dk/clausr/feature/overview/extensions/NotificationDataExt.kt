package dk.clausr.feature.overview.extensions

import java.text.Normalizer

private fun removeDiacritics(input: String): String {
    val normalized = Normalizer.normalize(input, Normalizer.Form.NFD)
    return normalized.replace(Regex("\\p{M}"), "")
}

internal val String.sluggify: String
    get() = removeDiacritics(this)
        .lowercase()
        .replace("[^a-zA-Z0-9 ]+".toRegex(), "")
        .replace(" ", "-")
