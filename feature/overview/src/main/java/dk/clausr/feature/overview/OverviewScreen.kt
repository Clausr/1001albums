package dk.clausr.feature.overview

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dk.clausr.a1001albumsgenerator.ui.components.AlbumThumb
import dk.clausr.a1001albumsgenerator.ui.components.LocalNavAnimatedVisibilityScope
import dk.clausr.a1001albumsgenerator.ui.components.LocalSharedTransitionScope
import dk.clausr.a1001albumsgenerator.ui.extensions.ignoreHorizontalParentPadding
import dk.clausr.core.common.android.openLink
import dk.clausr.core.common.extensions.formatToDate
import dk.clausr.core.data.workers.UpdateProjectWorker
import dk.clausr.core.data_widget.SerializedWidgetState
import dk.clausr.core.model.Project
import dk.clausr.core.model.StreamingPlatform
import dk.clausr.core.model.StreamingServices
import dk.clausr.core.model.UpdateFrequency
import dk.clausr.feature.overview.preview.albumPreviewData
import dk.clausr.feature.overview.preview.historicAlbumPreviewData
import kotlinx.collections.immutable.persistentListOf

@Composable
fun OverviewRoute(
    navigateToSettings: () -> Unit,
    navigateToAlbumDetails: (slug: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OverviewViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    OverviewScreen(
        modifier = modifier,
        state = uiState,
        navigateToSettings = navigateToSettings,
        navigateToAlbumDetails = navigateToAlbumDetails,
    )
}

@Composable
internal fun OverviewScreen(
    state: OverviewUiState,
    navigateToSettings: () -> Unit,
    navigateToAlbumDetails: (slug: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    with(LocalSharedTransitionScope.current) {
        Scaffold(
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            modifier = modifier,
            topBar = {
                with(LocalNavAnimatedVisibilityScope.current) {
                    TopAppBar(
                        modifier = Modifier
                            .renderInSharedTransitionScopeOverlay(zIndexInOverlay = 1f)
                            .animateEnterExit(enter = fadeIn() + slideInVertically(), exit = fadeOut() + slideOutVertically()),
                        title = { Text(text = "Your project") },
                        actions = {
                            IconButton(onClick = navigateToSettings) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Configure project",
                                )
                            }
                        },
                    )
                }
            },
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                when (state) {
                    OverviewUiState.Error -> Text("Error")
                    OverviewUiState.Loading -> Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }

                    is OverviewUiState.Success -> {
                        val prefStreamingPlatform =
                            (state.widgetState as? SerializedWidgetState.Success)?.data?.preferredStreamingPlatform ?: StreamingPlatform.Undefined

                        LazyVerticalGrid(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(
                                start = 16.dp,
                                end = 16.dp,
                                bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding(),
                            ),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            columns = GridCells.Fixed(count = 3),
                        ) {
                            item(
                                span = { GridItemSpan(maxLineSpan) },
                            ) {
                                state.currentAlbum?.let {
                                    BigCurrentAlbum(
                                        modifier = Modifier.padding(horizontal = 16.dp),
                                        state = state.widgetState,
                                        album = it,
                                        openLink = { url ->
                                            context.openLink(url)
                                        },
                                        startBurstUpdate = {
                                            UpdateProjectWorker.run(context = context, projectId = state.project.name)
                                        },
                                    )
                                }
                            }

                            if (state.didNotListen.isNotEmpty()) {
                                item(
                                    span = { GridItemSpan(maxLineSpan) },
                                ) {
                                    AlbumRow(
                                        modifier = Modifier
                                            .ignoreHorizontalParentPadding(16.dp)
                                            .fillMaxWidth(),
                                        title = "Did not listen",
                                        albums = state.didNotListen,
                                        onClickAlbum = navigateToAlbumDetails,
                                        streamingPlatform = state.streamingPlatform,
                                        tertiaryTextTransform = { historicAlbum ->
                                            historicAlbum.generatedAt.formatToDate()
                                        },
                                    )
                                }
                            }

                            if (state.topRated.isNotEmpty()) {
                                item(
                                    span = { GridItemSpan(maxLineSpan) },
                                ) {
                                    AlbumRow(
                                        modifier = Modifier
                                            .ignoreHorizontalParentPadding(16.dp)
                                            .fillMaxWidth(),
                                        title = "5⭐️ albums",
                                        albums = state.topRated,
                                        onClickAlbum = navigateToAlbumDetails,
                                        streamingPlatform = state.streamingPlatform,
                                        tertiaryTextTransform = { historicAlbum ->
                                            historicAlbum.generatedAt.formatToDate()
                                        },
                                    )
                                }
                            }

                            state.groupedHistory.forEach { (date, albums) ->
                                item(span = { GridItemSpan(maxLineSpan) }) {
                                    Text(
                                        text = date,
                                        modifier = Modifier.padding(top = 16.dp),
                                        style = MaterialTheme.typography.titleLarge,
                                    )
                                }

                                items(
                                    items = albums,
                                    key = { "history_${it.generatedAt}" },
                                ) { historicAlbum ->
                                    val streamingLink = StreamingServices.from(historicAlbum.album).getStreamingLinkFor(prefStreamingPlatform)

                                    val onClickPlay = streamingLink?.let {
                                        {
                                            context.openLink(streamingLink)
                                        }
                                    }

                                    AlbumThumb(
                                        album = historicAlbum,
                                        onClick = { navigateToAlbumDetails(historicAlbum.album.slug) },
                                        onClickPlay = onClickPlay,
                                        tertiaryText = historicAlbum.generatedAt.formatToDate(),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun OverviewPreview() {
    MaterialTheme {
        OverviewScreen(
            navigateToSettings = {},
            navigateToAlbumDetails = {},
            state = OverviewUiState.Success(
                project = Project(
                    name = "GlanceWidget",
                    currentAlbumSlug = "paranoid",
                    currentAlbumNotes = "",
                    updateFrequency = UpdateFrequency.DailyWithWeekends,
                    shareableUrl = "https://clausr.dk",
                    historicAlbums = listOf(
                        historicAlbumPreviewData(),
                    ),
                ),
                currentAlbum = albumPreviewData,
                widgetState = SerializedWidgetState.NotInitialized,
                didNotListen = persistentListOf(
                    historicAlbumPreviewData(),
                    historicAlbumPreviewData(),
                    historicAlbumPreviewData(),
                ),
                topRated = persistentListOf(),
                streamingPlatform = StreamingPlatform.Tidal,
                groupedHistory = mapOf(),
            ),
        )
    }
}
