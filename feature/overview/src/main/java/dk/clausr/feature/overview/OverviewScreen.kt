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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Badge
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dk.clausr.a1001albumsgenerator.ui.components.AlbumThumb
import dk.clausr.a1001albumsgenerator.ui.components.LocalNavAnimatedVisibilityScope
import dk.clausr.a1001albumsgenerator.ui.components.LocalSharedTransitionScope
import dk.clausr.a1001albumsgenerator.ui.extensions.ignoreHorizontalParentPadding
import dk.clausr.a1001albumsgenerator.ui.preview.PreviewSharedTransitionLayout
import dk.clausr.a1001albumsgenerator.ui.theme.OagTheme
import dk.clausr.core.common.extensions.collectWithLifecycle
import dk.clausr.core.common.extensions.formatToDate
import dk.clausr.core.data_widget.SerializedWidgetState
import dk.clausr.core.model.AlbumWidgetData
import dk.clausr.core.model.HistoricAlbum
import dk.clausr.core.model.Notification
import dk.clausr.core.model.NotificationType
import dk.clausr.core.model.Project
import dk.clausr.core.model.StreamingPlatform
import dk.clausr.core.model.StreamingServices
import dk.clausr.core.model.UpdateFrequency
import dk.clausr.extensions.askToAddToHomeScreen
import dk.clausr.feature.overview.preview.albumPreviewData
import dk.clausr.feature.overview.preview.historicAlbumPreviewData
import dk.clausr.worker.BurstUpdateWorker
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.launch
import java.time.Instant

@Composable
fun OverviewRoute(
    navigateToSettings: () -> Unit,
    navigateToAlbumDetails: (slug: String, listName: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OverviewViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    viewModel.viewEffect.collectWithLifecycle {
        when (it) {
            is OverviewViewModel.ViewEffect.ShowSnackbar -> {
                snackbarHostState.showSnackbar(message = it.message)
            }
        }
    }

    OverviewScreen(
        modifier = modifier,
        state = uiState,
        navigateToSettings = navigateToSettings,
        navigateToAlbumDetails = navigateToAlbumDetails,
        readAllNotifications = viewModel::clearUnreadNotifications,
        openLink = viewModel::openStreamingLink,
        snackbarHostState = snackbarHostState,
    )
}

@Composable
internal fun OverviewScreen(
    state: OverviewUiState,
    navigateToSettings: () -> Unit,
    navigateToAlbumDetails: (slug: String, listName: String) -> Unit,
    readAllNotifications: () -> Unit,
    openLink: (streamingLink: String) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = SnackbarHostState(),
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var showNotifications by remember {
        mutableStateOf(false)
    }

    with(LocalSharedTransitionScope.current) {
        Scaffold(
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState, modifier = Modifier.navigationBarsPadding())
            },
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            modifier = modifier,
            topBar = {
                with(LocalNavAnimatedVisibilityScope.current) {
                    TopAppBar(
                        modifier = Modifier
                            .renderInSharedTransitionScopeOverlay(zIndexInOverlay = 1f)
                            .animateEnterExit(enter = fadeIn() + slideInVertically(), exit = fadeOut() + slideOutVertically()),
                        title = { Text(text = stringResource(R.string.overview_app_bar_title)) },
                        actions = {
                            if (state is OverviewUiState.Success && !state.isUsingWidget) {
                                IconButton(
                                    onClick = {
                                        coroutineScope.launch {
                                            context.askToAddToHomeScreen()
                                        }
                                    },
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Star,
                                        contentDescription = null,
                                    )
                                }
                            }
                            Box {
                                IconButton(
                                    onClick = { showNotifications = true },
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Notifications,
                                        contentDescription = "Notifications",
                                    )
                                }
                                if (state is OverviewUiState.Success && state.notifications.isNotEmpty()) {
                                    Badge(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(4.dp),
                                    ) { Text(text = state.notifications.size.toString()) }
                                }
                            }
                            IconButton(onClick = navigateToSettings) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = stringResource(R.string.a11y_content_description_settings_icon),
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
                                        modifier = Modifier.ignoreHorizontalParentPadding(16.dp),
                                        state = state.widgetState,
                                        album = it,
                                        openLink = openLink,
                                        startBurstUpdate = {
                                            BurstUpdateWorker.enqueueUnique(context = context, projectId = state.project.name)
                                        },
                                    )
                                }
                            }

                            didNotListenSection(
                                state = state,
                                navigateToAlbumDetails = navigateToAlbumDetails,
                                clickPlay = openLink,
                            )

                            topRatedSection(
                                state = state,
                                navigateToAlbumDetails = navigateToAlbumDetails,
                                clickPlay = openLink,
                            )

                            historySection(
                                state = state,
                                prefStreamingPlatform = prefStreamingPlatform,
                                navigateToAlbumDetails = navigateToAlbumDetails,
                                onClickPlay = openLink,
                            )
                        }
                    }
                }
            }
        }

        if (state is OverviewUiState.Success) {
            NotificationUpperSheet(
                showNotifications = showNotifications,
                onDismiss = {
                    showNotifications = false
                },
                notifications = state.notifications,
                clearNotifications = readAllNotifications,
            )
        }
    }
}

private fun LazyGridScope.didNotListenSection(
    state: OverviewUiState.Success,
    navigateToAlbumDetails: (slug: String, listName: String) -> Unit,
    clickPlay: (String) -> Unit,
) {
    if (state.didNotListen.isNotEmpty()) {
        item(
            span = { GridItemSpan(maxLineSpan) },
        ) {
            AlbumRow(
                modifier = Modifier
                    .ignoreHorizontalParentPadding(16.dp)
                    .fillMaxWidth(),
                title = stringResource(R.string.overview_section_did_not_listen_title),
                albums = state.didNotListen,
                onClickAlbum = navigateToAlbumDetails,
                streamingPlatform = state.streamingPlatform,
                onClickPlay = clickPlay,
                tertiaryTextTransform = { historicAlbum ->
                    historicAlbum.metadata?.generatedAt?.formatToDate()
                },
            )
        }
    }
}

private fun LazyGridScope.topRatedSection(
    state: OverviewUiState.Success,
    navigateToAlbumDetails: (slug: String, listName: String) -> Unit,
    clickPlay: (String) -> Unit,
) {
    if (state.topRated.isNotEmpty()) {
        item(
            span = { GridItemSpan(maxLineSpan) },
        ) {
            AlbumRow(
                modifier = Modifier
                    .ignoreHorizontalParentPadding(16.dp)
                    .fillMaxWidth(),
                title = stringResource(R.string.overview_section_top_rated_albums_title),
                albums = state.topRated,
                onClickAlbum = navigateToAlbumDetails,
                streamingPlatform = state.streamingPlatform,
                tertiaryTextTransform = { historicAlbum ->
                    historicAlbum.metadata?.generatedAt?.formatToDate()
                },
                onClickPlay = clickPlay,
            )
        }
    }
}

private fun LazyGridScope.historySection(
    state: OverviewUiState.Success,
    prefStreamingPlatform: StreamingPlatform,
    navigateToAlbumDetails: (slug: String, listName: String) -> Unit,
    onClickPlay: (String) -> Unit,
) {
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
            key = { "history_${it.metadata?.generatedAt}" },
        ) { historicAlbum ->
            val streamingLink = StreamingServices.from(historicAlbum.album).getStreamingLinkFor(prefStreamingPlatform)

            val onPlay = streamingLink?.let {
                {
                    onClickPlay(it)
                }
            }

            AlbumThumb(
                album = historicAlbum,
                onClick = { navigateToAlbumDetails(historicAlbum.album.slug, "history-$date") },
                onClickPlay = onPlay,
                tertiaryText = historicAlbum.metadata?.generatedAt?.formatToDate(),
                listName = "history-$date",
            )
        }
    }
}

@Preview
@Composable
private fun OverviewPreview() {
    fun createPreviewAlbums(count: Int = 3): ImmutableList<HistoricAlbum> {
        return List(count) {
            historicAlbumPreviewData(slug = "$it")
        }.toPersistentList()
    }

    OagTheme {
        PreviewSharedTransitionLayout {
            OverviewScreen(
                navigateToSettings = {},
                navigateToAlbumDetails = { _, _ -> },
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
                    currentAlbum = albumPreviewData(),
                    widgetState = SerializedWidgetState.Success(
                        data = AlbumWidgetData(
                            "",
                            false,
                            "",
                            StreamingServices.NONE,
                            preferredStreamingPlatform = StreamingPlatform.Tidal,
                            unreadNotifications = 0
                        ),
                        currentProjectId = "oag_user"
                    ),
                    didNotListen = createPreviewAlbums(),
                    topRated = createPreviewAlbums(),
                    streamingPlatform = StreamingPlatform.Tidal,
                    groupedHistory = persistentMapOf(
                        "2024" to createPreviewAlbums(2),
                        "2023" to createPreviewAlbums(1),
                    ),
                    notifications = persistentListOf(
                        Notification(
                            id = "",
                            project = "",
                            createdAt = Instant.now(),
                            read = false,
                            type = NotificationType.Unknown,
                            data = null,
                            version = 1,
                        ),
                    ),
                    isUsingWidget = false,
                ),
                readAllNotifications = {},
                openLink = {},
            )
        }
    }
}
