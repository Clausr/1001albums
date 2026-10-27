package dk.clausr.feature.overview.search

import dk.clausr.core.model.Album
import dk.clausr.core.model.HistoricAlbum
import dk.clausr.core.model.Metadata
import dk.clausr.core.model.Rating
import kotlinx.collections.immutable.persistentListOf
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AlbumSearchTest {
    private val homogenic = album(
        slug = "homogenic",
        artist = "Björk",
        name = "Homogenic",
        year = "1997",
        rating = Rating.Rated(5),
        genres = listOf("electronic", "pop"),
        subGenres = listOf("art-pop", "trip-hop"),
        review = "Cold and huge. The strings on Jóga still give me goosebumps every single time.",
    )
    private val agaetis = album(
        slug = "agaetis-byrjun",
        artist = "Sigur Rós",
        name = "Ágætis byrjun",
        year = "1999",
        rating = Rating.Rated(4),
        genres = listOf("rock"),
        subGenres = listOf("post-rock", "dream-pop"),
    )
    private val folsom = album(
        slug = "at-folsom-prison",
        artist = "Johnny Cash",
        name = "At Folsom Prison",
        year = "1968",
        rating = Rating.DidNotListen,
        genres = listOf("country"),
        subGenres = listOf("outlaw-country"),
    )
    private val axis = album(
        slug = "axis-bold-as-love",
        artist = "The Jimi Hendrix Experience",
        name = "Axis: Bold As Love",
        year = "1967",
        rating = Rating.Unrated,
        genres = listOf("rock", "psychedelic-rock"),
        subGenres = listOf("acid-rock"),
    )
    private val current = album(slug = "current", artist = "Current", name = "Not generated yet", year = "2001", metadata = false)

    private val albums = listOf(homogenic, agaetis, folsom, axis, current)

    private fun search(filters: SearchFilters) = searchAlbums(albums, filters).map { it.album.album.slug }

    @Test
    fun `empty search returns every generated album in the given order`() {
        assertEquals(listOf("homogenic", "agaetis-byrjun", "at-folsom-prison", "axis-bold-as-love"), search(SearchFilters()))
    }

    @Test
    fun `query ignores case and accents`() {
        assertEquals(listOf("homogenic"), search(SearchFilters(query = "BJORK")))
        assertEquals(listOf("agaetis-byrjun"), search(SearchFilters(query = "sigur ros")))
        assertEquals(listOf("agaetis-byrjun"), search(SearchFilters(query = "agaetis")))
    }

    @Test
    fun `every word must match but may match different fields`() {
        assertEquals(listOf("at-folsom-prison"), search(SearchFilters(query = "cash prison")))
        assertEquals(emptyList(), search(SearchFilters(query = "cash homogenic")))
    }

    @Test
    fun `four digit word matches the release year`() {
        assertEquals(listOf("at-folsom-prison"), search(SearchFilters(query = "1968")))
        assertEquals(listOf("axis-bold-as-love"), search(SearchFilters(query = "hendrix 1967")))
        assertEquals(emptyList(), search(SearchFilters(query = "196")))
    }

    @Test
    fun `matching artist or name gives no snippet`() {
        val result = searchAlbums(albums, SearchFilters(query = "bjork")).single()
        assertNull(result.snippet)
    }

    @Test
    fun `matching the review gives an excerpt around the match`() {
        val result = searchAlbums(albums, SearchFilters(query = "goosebumps")).single()
        val snippet = result.snippet as MatchSnippet.Review
        assertTrue("goosebumps" in snippet.excerpt, snippet.excerpt)
    }

    @Test
    fun `review excerpt keeps the original accents`() {
        val result = searchAlbums(albums, SearchFilters(query = "joga")).single()
        val snippet = result.snippet as MatchSnippet.Review
        assertTrue("Jóga" in snippet.excerpt, snippet.excerpt)
    }

    @Test
    fun `long review excerpt is cut at word boundaries`() {
        val review = "word ".repeat(40) + "needle" + " word".repeat(40)
        val album = album(slug = "long", artist = "A", name = "B", year = "2000", review = review)
        val snippet = searchAlbums(listOf(album), SearchFilters(query = "needle")).single().snippet as MatchSnippet.Review
        assertTrue(snippet.excerpt.startsWith("…word"), snippet.excerpt)
        assertTrue(snippet.excerpt.endsWith("word…"), snippet.excerpt)
        assertTrue("needle" in snippet.excerpt)
    }

    @Test
    fun `query matches subgenres with spaces instead of hyphens`() {
        val result = searchAlbums(albums, SearchFilters(query = "outlaw country")).single()
        assertEquals("at-folsom-prison", result.album.album.slug)
        assertEquals(MatchSnippet.SubGenres(persistentListOf("Outlaw country")), result.snippet)
    }

    @Test
    fun `query does not match top level genres`() {
        assertEquals(emptyList(), search(SearchFilters(query = "electronic")))
    }

    @Test
    fun `ratings within the filter widen the result`() {
        assertEquals(
            listOf("homogenic", "agaetis-byrjun"),
            search(SearchFilters(ratings = setOf(RatingOption.FiveStars, RatingOption.FourStars))),
        )
        assertEquals(listOf("at-folsom-prison"), search(SearchFilters(ratings = setOf(RatingOption.DidNotListen))))
        assertEquals(listOf("axis-bold-as-love"), search(SearchFilters(ratings = setOf(RatingOption.Unrated))))
    }

    @Test
    fun `album matches a genre filter if any of its genres is selected`() {
        assertEquals(
            listOf("homogenic", "agaetis-byrjun", "axis-bold-as-love"),
            search(SearchFilters(genres = setOf("pop", "rock"))),
        )
    }

    @Test
    fun `different filter types narrow each other`() {
        assertEquals(
            listOf("agaetis-byrjun"),
            search(SearchFilters(decades = setOf(1990), genres = setOf("rock"))),
        )
        assertEquals(
            listOf("axis-bold-as-love"),
            search(SearchFilters(query = "love", decades = setOf(1960), genres = setOf("rock"))),
        )
    }

    @Test
    fun `options only include values present in generated albums`() {
        val options = filterOptions(albums, SearchFilters())
        assertEquals(listOf(1960, 1990), options.decades.map { it.value })
        assertEquals(
            listOf(RatingOption.FiveStars, RatingOption.FourStars, RatingOption.DidNotListen, RatingOption.Unrated),
            options.ratings.map { it.value },
        )
        assertEquals(listOf("country", "electronic", "pop", "psychedelic-rock", "rock"), options.genres.map { it.value })
    }

    @Test
    fun `option counts ignore their own filter type but respect the others`() {
        val options = filterOptions(albums, SearchFilters(decades = setOf(1960), genres = setOf("rock")))
        // Decades are counted with only the rock filter applied: Ágætis byrjun and Axis
        assertEquals(listOf(FilterOption(1960, 1), FilterOption(1990, 1)), options.decades)
        // Genres are counted with only the 1960s filter applied: At Folsom Prison and Axis
        assertEquals(1, options.genres.single { it.value == "rock" }.count)
        assertEquals(1, options.genres.single { it.value == "country" }.count)
        assertEquals(0, options.genres.single { it.value == "pop" }.count)
        // Ratings are counted with both filters applied: Axis
        assertEquals(1, options.ratings.single { it.value == RatingOption.Unrated }.count)
        assertEquals(0, options.ratings.single { it.value == RatingOption.FiveStars }.count)
    }

    @Test
    fun `genre label replaces hyphens and capitalises`() {
        assertEquals("Hard rock", genreLabel("hard-rock"))
        assertEquals("Hip hop", genreLabel("hip-hop"))
    }

    private fun album(
        slug: String,
        artist: String,
        name: String,
        year: String,
        rating: Rating = Rating.Unrated,
        genres: List<String> = emptyList(),
        subGenres: List<String> = emptyList(),
        review: String = "",
        metadata: Boolean = true,
    ) = HistoricAlbum(
        album = Album(
            id = slug,
            artist = artist,
            artistOrigin = null,
            imageUrl = "",
            genres = genres,
            subGenres = subGenres,
            name = name,
            slug = slug,
            releaseDate = year,
            globalReviewsUrl = "",
            wikipediaUrl = "",
            spotifyId = null,
            appleMusicId = null,
            tidalId = null,
            amazonMusicId = null,
            youtubeMusicId = null,
            deezerId = null,
            qobuzId = null,
        ),
        metadata = if (metadata) {
            Metadata(
                rating = rating,
                review = review,
                generatedAt = Instant.EPOCH,
                globalRating = 0.0,
                isRevealed = true,
            )
        } else {
            null
        },
    )
}
