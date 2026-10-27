package dk.clausr.feature.overview.search

import dk.clausr.core.model.HistoricAlbum
import dk.clausr.core.model.Rating
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.Serializable
import java.text.Normalizer
import java.util.Locale

enum class RatingOption(val stars: Int?) {
    FiveStars(5),
    FourStars(4),
    ThreeStars(3),
    TwoStars(2),
    OneStar(1),
    DidNotListen(null),
    Unrated(null),
}

/**
 * Different filter types narrow each other down, while several values within one type widen the result.
 * An empty set means the filter type isn't in use.
 */
@Serializable
data class SearchFilters(
    val query: String = "",
    val ratings: Set<RatingOption> = emptySet(),
    val decades: Set<Int> = emptySet(),
    val genres: Set<String> = emptySet(),
) {
    val hasActiveFilters: Boolean
        get() = ratings.isNotEmpty() || decades.isNotEmpty() || genres.isNotEmpty()
}

/** Why an album matched, when it isn't obvious from the artist, name or year shown in the row. */
sealed interface MatchSnippet {
    data class Review(val excerpt: String) : MatchSnippet
    data class SubGenres(val subGenres: ImmutableList<String>) : MatchSnippet
}

data class SearchResult(
    val album: HistoricAlbum,
    val snippet: MatchSnippet?,
)

data class FilterOption<T>(
    val value: T,
    val count: Int,
)

data class FilterOptions(
    val ratings: ImmutableList<FilterOption<RatingOption>>,
    val decades: ImmutableList<FilterOption<Int>>,
    val genres: ImmutableList<FilterOption<String>>,
)

/**
 * Searches the albums already in the history; albums without metadata (not yet generated) are left out.
 *
 * Every word of the query has to be found somewhere: in the artist, album name, the user's own review or a subgenre.
 * A four digit word also matches the release year. Matching ignores case, accents and hyphens.
 * The order of [albums] is kept.
 */
fun searchAlbums(
    albums: List<HistoricAlbum>,
    filters: SearchFilters,
): List<SearchResult> {
    val terms = filters.query.searchTerms()
    return albums
        .filter { it.metadata != null && it.matchesFilters(filters) }
        .mapNotNull { album -> album.match(terms) }
}

/**
 * The options offered for each filter type, counted against the query and the *other* filter types,
 * so a count tells how many results selecting that option would add.
 * Only options present in [albums] are offered.
 */
fun filterOptions(
    albums: List<HistoricAlbum>,
    filters: SearchFilters,
): FilterOptions {
    val historic = albums.filter { it.metadata != null }
    val terms = filters.query.searchTerms()
    val matchingQuery = historic.filter { it.match(terms) != null }

    val ratingCounts = matchingQuery
        .filter { it.matchesFilters(filters.copy(ratings = emptySet())) }
        .groupingBy { it.ratingOption() }
        .eachCount()
    val decadeCounts = matchingQuery
        .filter { it.matchesFilters(filters.copy(decades = emptySet())) }
        .mapNotNull { it.decade() }
        .groupingBy { it }
        .eachCount()
    val genreCounts = matchingQuery
        .filter { it.matchesFilters(filters.copy(genres = emptySet())) }
        .flatMap { it.album.genres.distinct() }
        .groupingBy { it }
        .eachCount()

    val presentRatings = historic.mapTo(mutableSetOf()) { it.ratingOption() }
    return FilterOptions(
        ratings = RatingOption.entries
            .filter { it in presentRatings }
            .map { FilterOption(it, ratingCounts[it] ?: 0) }
            .toImmutableList(),
        decades = historic.mapNotNullTo(sortedSetOf()) { it.decade() }
            .map { FilterOption(it, decadeCounts[it] ?: 0) }
            .toImmutableList(),
        genres = historic.flatMapTo(mutableSetOf()) { it.album.genres }
            .sortedBy { genreLabel(it) }
            .map { FilterOption(it, genreCounts[it] ?: 0) }
            .toImmutableList(),
    )
}

/** `hard-rock` → `Hard rock` */
fun genreLabel(genre: String): String = genre.replace('-', ' ').replaceFirstChar { it.titlecase(Locale.ROOT) }

fun HistoricAlbum.ratingOption(): RatingOption = when (val rating = metadata?.rating) {
    is Rating.Rated -> RatingOption.entries.firstOrNull { it.stars == rating.rating } ?: RatingOption.Unrated
    Rating.DidNotListen -> RatingOption.DidNotListen
    Rating.Unrated, null -> RatingOption.Unrated
}

fun HistoricAlbum.releaseYear(): Int? = album.releaseDate.take(YEAR_LENGTH).toIntOrNull()

fun HistoricAlbum.decade(): Int? = releaseYear()?.let { it / 10 * 10 }

private fun HistoricAlbum.matchesFilters(filters: SearchFilters): Boolean {
    val matchesRating = filters.ratings.isEmpty() || ratingOption() in filters.ratings
    val matchesDecade = filters.decades.isEmpty() || decade() in filters.decades
    val matchesGenre = filters.genres.isEmpty() || album.genres.any { it in filters.genres }
    return matchesRating && matchesDecade && matchesGenre
}

private fun HistoricAlbum.match(terms: List<String>): SearchResult? {
    if (terms.isEmpty()) return SearchResult(this, snippet = null)

    val primary = listOf(album.artist, album.name).map { it.folded().text }
    val year = releaseYear()?.toString()
    val review = metadata?.review.orEmpty().folded()
    val subGenres = album.subGenres.map { it to it.folded().text }

    var reviewMatch: Pair<Int, Int>? = null
    val matchedSubGenres = linkedSetOf<String>()
    for (term in terms) {
        if (primary.any { term in it } || term == year) continue

        val reviewIndex = review.text.indexOf(term)
        val subGenreHits = subGenres.filter { (_, folded) -> term in folded }.map { it.first }
        if (reviewIndex < 0 && subGenreHits.isEmpty()) return null

        if (reviewIndex >= 0 && reviewMatch == null) reviewMatch = reviewIndex to term.length
        matchedSubGenres += subGenreHits
    }

    val snippet = reviewMatch
        ?.let { (index, length) -> MatchSnippet.Review(excerpt(metadata?.review.orEmpty(), review, index, length)) }
        ?: matchedSubGenres.takeIf { it.isNotEmpty() }?.let { MatchSnippet.SubGenres(it.map(::genreLabel).toImmutableList()) }
    return SearchResult(this, snippet)
}

private fun String.searchTerms(): List<String> = folded().text.split(' ').filter { it.isNotBlank() }

/** Text folded for matching, with the index in the original string of each folded character. */
private class FoldedText(val text: String, val originalIndices: IntArray)

private val combiningMarks = Regex("\\p{Mn}+")

/** Letters that aren't a base letter plus an accent, so they don't fold by removing accents. */
private val letterReplacements = mapOf(
    'æ' to "ae",
    'ø' to "o",
    'œ' to "oe",
    'ß' to "ss",
    'ł' to "l",
    'đ' to "d",
    'ð' to "d",
    'þ' to "th",
)

private fun String.folded(): FoldedText {
    val text = StringBuilder(length)
    val indices = ArrayList<Int>(length)
    forEachIndexed { index, char ->
        val lowercase = char.lowercaseChar()
        val folded = when {
            char == '-' || char.isWhitespace() -> " "
            lowercase in letterReplacements -> letterReplacements.getValue(lowercase)
            else -> Normalizer.normalize(char.toString().lowercase(Locale.ROOT), Normalizer.Form.NFD)
                .replace(combiningMarks, "")
        }
        folded.forEach {
            text.append(it)
            indices += index
        }
    }
    return FoldedText(text.toString(), indices.toIntArray())
}

private const val YEAR_LENGTH = 4
private const val EXCERPT_CONTEXT_BEFORE = 30
private const val EXCERPT_CONTEXT_AFTER = 60

/** A part of [original] around the match, cut at word boundaries. */
private fun excerpt(
    original: String,
    folded: FoldedText,
    foldedIndex: Int,
    foldedLength: Int,
): String {
    val matchStart = folded.originalIndices[foldedIndex]
    val matchEnd = folded.originalIndices[foldedIndex + foldedLength - 1] + 1

    var start = (matchStart - EXCERPT_CONTEXT_BEFORE).coerceAtLeast(0)
    if (start > 0) {
        start = original.indexOf(' ', start).takeIf { it in start until matchStart }?.plus(1) ?: start
    }
    var end = (matchEnd + EXCERPT_CONTEXT_AFTER).coerceAtMost(original.length)
    if (end < original.length) {
        end = original.lastIndexOf(' ', end).takeIf { it >= matchEnd } ?: end
    }

    val prefix = if (start > 0) "…" else ""
    val suffix = if (end < original.length) "…" else ""
    return prefix + original.substring(start, end).replace(Regex("\\s+"), " ").trim() + suffix
}
