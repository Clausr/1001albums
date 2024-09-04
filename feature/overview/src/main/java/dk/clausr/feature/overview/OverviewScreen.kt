package dk.clausr.feature.overview

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import dk.clausr.core.model.NotificationData
import dk.clausr.core.model.NotificationResponse
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
    navigateToAlbumDetails: (slug: String, listName: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OverviewViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()

    OverviewScreen(
        modifier = modifier,
        state = uiState,
        navigateToSettings = navigateToSettings,
        navigateToAlbumDetails = navigateToAlbumDetails,
        notifications = notifications,
    )
}

@Composable
internal fun OverviewScreen(
    state: OverviewUiState,
    notifications: List<NotificationResponse>,
    navigateToSettings: () -> Unit,
    navigateToAlbumDetails: (slug: String, listName: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var showNotifications by remember {
        mutableStateOf(false)
    }
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
                            Box {
                                if (notifications.isNotEmpty()) {
                                    Badge(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(4.dp),
                                    ) { Text(text = notifications.size.toString()) }
                                }
                                IconButton(
                                    onClick = { showNotifications = true },
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Notifications,
                                        contentDescription = "Notifications",
                                    )
                                }
                            }
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
                                        modifier = Modifier.ignoreHorizontalParentPadding(16.dp),
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
                                        onClick = { navigateToAlbumDetails(historicAlbum.album.slug, "history-$date") },
                                        onClickPlay = onClickPlay,
                                        tertiaryText = historicAlbum.generatedAt.formatToDate(),
                                        listName = "history-$date",
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = showNotifications,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            BackHandler { showNotifications = false }

            Box(
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.2f))
                    .clickable(interactionSource = null, indication = null, onClick = { showNotifications = false }),

                )
        }

        AnimatedVisibility(
            visible = showNotifications,
            enter = slideInVertically(initialOffsetY = { -it }),
            exit = slideOutVertically(targetOffsetY = { -it }),
        ) {
            Surface(
                shape = MaterialTheme.shapes.medium.copy(topStart = CornerSize(0.dp), topEnd = CornerSize(0.dp)),
                shadowElevation = 2.dp,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Notifications",
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        IconButton(onClick = { showNotifications = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    notifications.forEach { notification ->
                        Column {
                            Text(text = notification.type.toString(), style = MaterialTheme.typography.labelLarge)
                            val textToShow: String = when (val data = notification.data) {
                                is NotificationData.AlbumsRatedData -> {
                                    "You've rated ${data.numberOfAlbums} albums!"
                                }

                                is NotificationData.CustomData -> data.body
                                is NotificationData.DonationPushData -> {
                                    "Plz donate"
                                }

                                is NotificationData.GroupAlbumsGeneratedData -> {
                                    "Your group ${data.groupSlug} has reached ${data.numberOfAlbums} albums!"
                                }

                                is NotificationData.GroupReviewData -> {
                                    "${data.projectName} just gave ${data.albumName} ${data.rating} stars!"
                                }

                                is NotificationData.NewGroupMemberData -> TODO()
                                is NotificationData.ReviewThumbUpData -> TODO()
                                is NotificationData.SignupData -> TODO()
                                NotificationData.Unknown -> TODO()
                                null -> TODO()
                            }
                            Text(textToShow)
                            Text(text = "at ${notification.createdAt}")
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
            notifications = emptyList(),
        )
    }
}
