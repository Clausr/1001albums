package dk.clausr.feature.overview.search

import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dk.clausr.a1001albumsgenerator.ui.components.AlbumCover
import dk.clausr.a1001albumsgenerator.ui.components.LocalNavAnimatedVisibilityScope
import dk.clausr.a1001albumsgenerator.ui.components.LocalSharedTransitionScope
import dk.clausr.a1001albumsgenerator.ui.extensions.TrackScreenViewEvent
import dk.clausr.a1001albumsgenerator.ui.preview.PreviewSharedTransitionLayout
import dk.clausr.a1001albumsgenerator.ui.theme.OagTheme
import dk.clausr.core.model.Rating
import dk.clausr.feature.overview.R
import dk.clausr.feature.overview.navigation.AlbumDetailsRoute
import dk.clausr.feature.overview.navigation.AlbumListSource
import dk.clausr.feature.overview.navigation.detailsRoute
import dk.clausr.feature.overview.preview.historicAlbumPreviewData
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

/** Prefix of the shared element keys, so the cover animates between the result and the details screen. */
private const val SEARCH_LIST_NAME = "search"

@Composable
fun AlbumSearchRoute(
    onNavigateBack: () -> Unit,
    navigateToAlbumDetails: (AlbumDetailsRoute) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AlbumSearchViewModel = hiltViewModel(),
) {
    TrackScreenViewEvent(screenName = "Album search")

    val query by viewModel.query.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AlbumSearchScreen(
        query = query,
        state = uiState,
        onQueryChange = viewModel::onQueryChange,
        onNavigateBack = onNavigateBack,
        onClickResult = { result ->
            navigateToAlbumDetails(result.album.detailsRoute(listName = SEARCH_LIST_NAME, source = AlbumListSource.None))
        },
        onToggleRating = viewModel::toggleRating,
        onToggleDecade = viewModel::toggleDecade,
        onToggleGenre = viewModel::toggleGenre,
        onClearFilters = viewModel::clearFilters,
        modifier = modifier,
    )
}

@Composable
internal fun AlbumSearchScreen(
    query: String,
    state: AlbumSearchUiState,
    onQueryChange: (String) -> Unit,
    onNavigateBack: () -> Unit,
    onClickResult: (SearchResult) -> Unit,
    onToggleRating: (RatingOption) -> Unit,
    onToggleDecade: (Int) -> Unit,
    onToggleGenre: (String) -> Unit,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    val listState = rememberLazyListState()
    var openFilter by rememberSaveable { mutableStateOf<FilterType?>(null) }

    // Only focus the field when the screen is opened, not when coming back from an album
    var hasRequestedFocus by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (!hasRequestedFocus) {
            focusRequester.requestFocus()
            hasRequestedFocus = true
        }
    }

    LaunchedEffect(listState.isScrollInProgress) {
        if (listState.isScrollInProgress) focusManager.clearFocus()
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            Column(modifier = Modifier.statusBarsPadding()) {
                SearchField(
                    query = query,
                    onQueryChange = onQueryChange,
                    onSearch = { focusManager.clearFocus() },
                    onNavigateBack = onNavigateBack,
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .focusRequester(focusRequester),
                )
                FilterChips(
                    filters = state.filters,
                    options = state.options,
                    onOpenFilter = {
                        // Otherwise the keyboard comes back when the sheet returns focus to the field
                        focusManager.clearFocus()
                        openFilter = it
                    },
                    onClearFilters = onClearFilters,
                )
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { innerPadding ->
        val results = state.results
        when {
            results == null -> Unit

            results.isEmpty() -> NoResults(
                showClearFilters = state.filters.hasActiveFilters,
                onClearFilters = onClearFilters,
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
            )

            else -> LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .consumeWindowInsets(innerPadding)
                    .imePadding(),
                contentPadding = innerPadding,
            ) {
                items(
                    items = results,
                    key = { "${it.album.album.slug}-${it.album.metadata?.generatedAt}" },
                ) { result ->
                    SearchResultRow(
                        result = result,
                        onClick = {
                            focusManager.clearFocus()
                            onClickResult(result)
                        },
                    )
                }
                item {
                    Box(modifier = Modifier.navigationBarsPadding())
                }
            }
        }
    }

    openFilter?.let { filterType ->
        FilterSheet(
            filterType = filterType,
            filters = state.filters,
            options = state.options,
            onToggleRating = onToggleRating,
            onToggleDecade = onToggleDecade,
            onToggleGenre = onToggleGenre,
            onDismiss = { openFilter = null },
        )
    }
}

@Composable
private fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = SearchBarDefaults.inputFieldShape,
        color = SearchBarDefaults.colors().containerColor,
    ) {
        SearchBarDefaults.InputField(
            query = query,
            onQueryChange = onQueryChange,
            onSearch = { onSearch() },
            // Always expanded; collapsing would clear the focus, and there's no overlay to expand into
            expanded = true,
            onExpandedChange = {},
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(text = stringResource(R.string.search_placeholder)) },
            leadingIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.a11y_search_back),
                    )
                }
            },
            trailingIcon = if (query.isNotEmpty()) {
                {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.a11y_search_clear_query),
                        )
                    }
                }
            } else {
                null
            },
        )
    }
}

@Composable
private fun FilterChips(
    filters: SearchFilters,
    options: FilterOptions,
    onOpenFilter: (FilterType) -> Unit,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FilterType.entries.forEach { filterType ->
            val selectedLabels = when (filterType) {
                FilterType.Rating -> options.ratings.map { it.value }.filter { it in filters.ratings }.map { ratingLabel(it) }
                FilterType.Decade -> options.decades.map { it.value }.filter { it in filters.decades }.map { decadeLabel(it) }
                FilterType.Genre -> options.genres.map { it.value }.filter { it in filters.genres }.map { genreLabel(it) }
            }
            FilterChip(
                selected = selectedLabels.isNotEmpty(),
                onClick = { onOpenFilter(filterType) },
                label = {
                    Text(
                        text = when (selectedLabels.size) {
                            0 -> filterTypeLabel(filterType)
                            1 -> selectedLabels.single()
                            else -> stringResource(R.string.search_filter_selection_more, selectedLabels.first(), selectedLabels.size - 1)
                        },
                    )
                },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                    )
                },
            )
        }
        if (filters.hasActiveFilters) {
            AssistChip(
                onClick = onClearFilters,
                label = { Text(text = stringResource(R.string.search_filter_clear)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                    )
                },
            )
        }
    }
}

@Composable
private fun FilterSheet(
    filterType: FilterType,
    filters: SearchFilters,
    options: FilterOptions,
    onToggleRating: (RatingOption) -> Unit,
    onToggleDecade: (Int) -> Unit,
    onToggleGenre: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val rows: ImmutableList<FilterRowData> = when (filterType) {
        FilterType.Rating -> options.ratings.map {
            FilterRowData(ratingLabel(it.value), it.count, it.value in filters.ratings) { onToggleRating(it.value) }
        }

        FilterType.Decade -> options.decades.map {
            FilterRowData(decadeLabel(it.value), it.count, it.value in filters.decades) { onToggleDecade(it.value) }
        }

        FilterType.Genre -> options.genres.map {
            FilterRowData(genreLabel(it.value), it.count, it.value in filters.genres) { onToggleGenre(it.value) }
        }
    }.toImmutableList()

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Text(
            text = filterTypeLabel(filterType),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
        )
        LazyColumn(contentPadding = PaddingValues(bottom = 16.dp)) {
            items(items = rows, key = { it.label }) { row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .toggleable(value = row.selected, role = Role.Checkbox, onValueChange = { row.onToggle() })
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(checked = row.selected, onCheckedChange = null, modifier = Modifier.padding(12.dp))
                    Text(text = row.label, modifier = Modifier.weight(1f))
                    Text(
                        text = row.count.toString(),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 12.dp),
                    )
                }
            }
        }
    }
}

private class FilterRowData(
    val label: String,
    val count: Int,
    val selected: Boolean,
    val onToggle: () -> Unit,
)

@Composable
private fun SearchResultRow(
    result: SearchResult,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val album = result.album.album
    val animatedVisibilityScope = LocalNavAnimatedVisibilityScope.current
    with(LocalSharedTransitionScope.current) {
        ListItem(
            modifier = modifier
                .sharedBounds(
                    sharedContentState = rememberSharedContentState(key = "$SEARCH_LIST_NAME-bounds-${album.slug}"),
                    animatedVisibilityScope = animatedVisibilityScope,
                    resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds,
                )
                .clickable(onClick = onClick),
            leadingContent = {
                AlbumCover(
                    coverUrl = album.imageUrl,
                    albumSlug = album.slug,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(56.dp)
                        .sharedElement(
                            sharedContentState = rememberSharedContentState(key = "$SEARCH_LIST_NAME-cover-${album.slug}"),
                            animatedVisibilityScope = animatedVisibilityScope,
                        ),
                    shape = RoundedCornerShape(4.dp),
                )
            },
            headlineContent = {
                Text(text = album.name, maxLines = 1, overflow = TextOverflow.Ellipsis)
            },
            supportingContent = {
                Column {
                    Text(
                        text = listOfNotNull(album.artist, result.album.releaseYear()?.toString()).joinToString(" · "),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    result.snippet?.let { snippet ->
                        Text(
                            text = when (snippet) {
                                is MatchSnippet.Review -> snippet.excerpt
                                is MatchSnippet.SubGenres -> snippet.subGenres.joinToString(", ")
                            },
                            fontStyle = if (snippet is MatchSnippet.Review) FontStyle.Italic else FontStyle.Normal,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            },
            trailingContent = {
                Column(horizontalAlignment = Alignment.End) {
                    val rating = result.album.ratingOption()
                    if (rating != RatingOption.Unrated) {
                        Text(text = ratingLabel(rating), style = MaterialTheme.typography.labelMedium)
                    }
                    if (album.isUserAlbum) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = stringResource(R.string.a11y_search_user_album),
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            },
        )
    }
}

@Composable
private fun NoResults(
    showClearFilters: Boolean,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(R.string.search_no_results),
            style = MaterialTheme.typography.titleMedium,
        )
        if (showClearFilters) {
            TextButton(onClick = onClearFilters) {
                Text(text = stringResource(R.string.search_clear_filters))
            }
        }
    }
}

@Composable
private fun filterTypeLabel(filterType: FilterType): String = stringResource(
    when (filterType) {
        FilterType.Rating -> R.string.search_filter_rating
        FilterType.Decade -> R.string.search_filter_decade
        FilterType.Genre -> R.string.search_filter_genre
    },
)

@Composable
private fun ratingLabel(rating: RatingOption): String = when (rating) {
    RatingOption.DidNotListen -> stringResource(R.string.rating_did_not_listen)
    RatingOption.Unrated -> stringResource(R.string.rating_text_unrated)
    else -> stringResource(R.string.rating_text_rated, requireNotNull(rating.stars))
}

@Composable
private fun decadeLabel(decade: Int): String = stringResource(R.string.search_decade, decade)

@Preview
@Composable
private fun AlbumSearchScreenPreview() {
    val albums = persistentListOf(
        historicAlbumPreviewData(slug = "paranoid"),
        historicAlbumPreviewData(slug = "master-of-reality", rating = Rating.Rated(4)),
        historicAlbumPreviewData(slug = "vol-4", rating = Rating.DidNotListen),
    )
    val filters = SearchFilters(ratings = setOf(RatingOption.FiveStars, RatingOption.FourStars))
    OagTheme {
        PreviewSharedTransitionLayout {
            AlbumSearchScreen(
                query = "",
                state = AlbumSearchUiState(
                    filters = filters,
                    results = searchAlbums(albums, filters).toImmutableList(),
                    options = filterOptions(albums, filters),
                ),
                onQueryChange = {},
                onNavigateBack = {},
                onClickResult = {},
                onToggleRating = {},
                onToggleDecade = {},
                onToggleGenre = {},
                onClearFilters = {},
            )
        }
    }
}
