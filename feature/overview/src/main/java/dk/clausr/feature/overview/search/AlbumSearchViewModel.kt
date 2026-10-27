package dk.clausr.feature.overview.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dk.clausr.a1001albumsgenerator.analytics.AnalyticsHelper
import dk.clausr.a1001albumsgenerator.ui.extensions.logSearchFilterUsed
import dk.clausr.core.data.repository.OagRepository
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.serialization.json.Json
import javax.inject.Inject

enum class FilterType {
    Rating,
    Decade,
    Genre,
}

@HiltViewModel
class AlbumSearchViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val analyticsHelper: AnalyticsHelper,
    oagRepository: OagRepository,
) : ViewModel() {
    private val _query = MutableStateFlow(savedStateHandle.get<String>(QUERY_KEY).orEmpty())

    /** Updated on every keystroke, unlike the query the results are based on. */
    val query: StateFlow<String> = _query.asStateFlow()

    /** The chip filters; the query lives in [_query]. */
    private val filters = MutableStateFlow(
        savedStateHandle.get<String>(FILTERS_KEY)?.let { Json.decodeFromString<SearchFilters>(it) } ?: SearchFilters(),
    )

    private val loggedFilterTypes = mutableSetOf<FilterType>()

    @OptIn(FlowPreview::class)
    val uiState: StateFlow<AlbumSearchUiState> = combine(
        oagRepository.historicAlbums,
        _query.debounce { if (it.isBlank()) 0L else QUERY_DEBOUNCE_MILLIS },
        filters,
    ) { albums, query, filters ->
        val appliedFilters = filters.copy(query = query)
        AlbumSearchUiState(
            filters = filters,
            results = searchAlbums(albums, appliedFilters).toImmutableList(),
            options = filterOptions(albums, appliedFilters),
        )
    }
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AlbumSearchUiState(),
        )

    fun onQueryChange(query: String) {
        _query.value = query
        savedStateHandle[QUERY_KEY] = query
    }

    fun toggleRating(rating: RatingOption) = updateFilters(FilterType.Rating) { it.copy(ratings = it.ratings.toggle(rating)) }

    fun toggleDecade(decade: Int) = updateFilters(FilterType.Decade) { it.copy(decades = it.decades.toggle(decade)) }

    fun toggleGenre(genre: String) = updateFilters(FilterType.Genre) { it.copy(genres = it.genres.toggle(genre)) }

    fun clearFilters() = updateFilters(filterType = null) { SearchFilters() }

    private fun updateFilters(
        filterType: FilterType?,
        transform: (SearchFilters) -> SearchFilters,
    ) {
        val updated = filters.updateAndGet(transform)
        savedStateHandle[FILTERS_KEY] = Json.encodeToString(updated)

        // Only which types of filters are used is logged, once per type per search
        if (filterType != null && loggedFilterTypes.add(filterType)) {
            analyticsHelper.logSearchFilterUsed(filterType.name.lowercase())
        }
    }

    private fun <T> Set<T>.toggle(value: T): Set<T> = if (value in this) this - value else this + value

    private companion object {
        const val QUERY_KEY = "query"
        const val FILTERS_KEY = "filters"
        const val QUERY_DEBOUNCE_MILLIS = 300L
    }
}

/**
 * @param results null until the first search has run.
 */
data class AlbumSearchUiState(
    val filters: SearchFilters = SearchFilters(),
    val results: ImmutableList<SearchResult>? = null,
    val options: FilterOptions = FilterOptions(persistentListOf(), persistentListOf(), persistentListOf()),
)
