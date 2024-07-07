package dk.clausr.feature.overview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import dk.clausr.core.model.Album
import dk.clausr.core.model.HistoricAlbum
import dk.clausr.core.model.Project
import dk.clausr.core.model.Rating
import dk.clausr.core.model.UpdateFrequency
import timber.log.Timber
import java.time.Instant

@Composable
fun OverviewRoute(
    modifier: Modifier = Modifier,
    onConfigureWidget: () -> Unit,
    viewModel: OverviewViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Timber.d("Overview route running -- $uiState")
    LaunchedEffect(uiState) {
        if (uiState is OverviewUiState.NoProject) {
            onConfigureWidget()
        }
    }

    OverviewScreen(
        modifier = modifier,
        state = uiState,
        onConfigureWidget = onConfigureWidget,
    )
}

@Composable
internal fun OverviewScreen(
    state: OverviewUiState,
    onConfigureWidget: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(text = "Your project") },
                actions = {
                    IconButton(onClick = onConfigureWidget) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Configure project",
                        )
                    }
                })
        },
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            when (state) {
                OverviewUiState.Error -> Text("Error")
                OverviewUiState.NoProject -> {
                    Text("No project yet mate")
                }
                OverviewUiState.Loading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }

                is OverviewUiState.Success -> {
                    var showOnlyDnl by remember {
                        mutableStateOf(false)
                    }

                    val history by remember(showOnlyDnl) {
                        mutableStateOf(state.project.historicAlbums.filter {
                            if (showOnlyDnl) {
                                it.rating !is Rating.Rated
                            } else true
                        })
                    }

                    val expandedItems = remember { mutableStateListOf<String>() }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = WindowInsets.navigationBars.asPaddingValues(),
                    ) {
                        item {
                            state.currentAlbum?.let {
                                // TODO Create interactions / Prefill rating thing
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        AsyncImage(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .aspectRatio(1f)
                                                .background(MaterialTheme.colorScheme.primaryContainer),
                                            model = it.imageUrl,
                                            contentDescription = "Cover"
                                        )
                                    }
                                    Text(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 8.dp),
                                        text = it.artist,
                                        textAlign = TextAlign.Center,
                                        style = MaterialTheme.typography.titleLarge,
                                    )
                                    Text(
                                        modifier = Modifier.fillMaxWidth(),
                                        text = it.name,
                                        textAlign = TextAlign.Center,
                                        style = MaterialTheme.typography.titleMedium,
                                    )
                                }
                            }
                        }

                        if (state.project.historicAlbums.isNotEmpty()) {
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        modifier = Modifier.weight(1f),
                                        text = stringResource(
                                            id = R.string.history_header,
                                            if (showOnlyDnl) {
                                                history.size
                                            } else {
                                                history.count { it.rating is Rating.Rated }
                                            },
                                            state.project.historicAlbums.size
                                        ),
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    )

                                    TextButton(onClick = { showOnlyDnl = !showOnlyDnl }) {
                                        Text(if (showOnlyDnl) "Show all" else "Show DNL")
                                    }
                                }
                            }
                        }
                        items(
                            items = history,
                            key = { it.generatedAt }) { historicAlbum ->
                            val slug = historicAlbum.album.slug
                            HistoricAlbumCard(
                                historicAlbum = historicAlbum,
                                modifier = Modifier.padding(horizontal = 16.dp),
                                expanded = slug in expandedItems,
                                onClick = {
                                    if (expandedItems.contains(slug)) {
                                        expandedItems.remove(slug)
                                    } else {
                                        expandedItems.add(slug)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }


}

@Composable
private fun HistoricAlbumCard(
    historicAlbum: HistoricAlbum,
    modifier: Modifier = Modifier,
    expanded: Boolean = false,
    onClick: () -> Unit,
) {
    val album = historicAlbum.album
    Card(
        modifier = modifier,
//        onClick = onClick,
        shape = RoundedCornerShape(
            topStart = 0.dp,
            bottomStart = 0.dp,
            bottomEnd = 8.dp,
            topEnd = 8.dp
        ),
    ) {
        // TODO Either shared transition or something else fancyish

        Row(
            modifier = Modifier.fillMaxWidth(),
        ) {
            AsyncImage(
                modifier = Modifier.size(100.dp),
                model = ImageRequest.Builder(
                    LocalContext.current
                ).data(album.imageUrl).crossfade(true).build(),
                contentDescription = "cover",
                contentScale = ContentScale.FillWidth,
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 8.dp, horizontal = 8.dp)
            ) {
                Text(
                    text = album.artist,
                )
                Text(
                    text = album.name,
                    style = MaterialTheme.typography.labelLarge
                )
                Text(
                    text = historicAlbum.review,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic)
                )

//                AnimatedContent(targetState = expanded) { isExpanded ->
//                    if (isExpanded) {
//                        Row(Modifier.fillMaxWidth()) {
//                            IconButton(onClick = { /*TODO*/ }) {
//
//                            }
//                        }
//                    } else {
//
//                    }
//                }
            }
            val rating = when (val rating = historicAlbum.rating) {
                is Rating.Rated -> rating.rating.toString()
                Rating.Unrated -> "Unrated"
                Rating.DidNotListen -> "DNL"
            }
            Text(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .align(Alignment.CenterVertically),
                text = rating,
                style = MaterialTheme.typography.headlineMedium
            )
        }

    }
}


@Preview
@Composable
private fun OverviewPreview() {
    MaterialTheme {
        OverviewScreen(
            onConfigureWidget = {},
            state = OverviewUiState.Success(
                project = Project(
                    name = "GlanceWidget",
                    currentAlbumSlug = "paranoid",
                    currentAlbumNotes = "",
                    updateFrequency = UpdateFrequency.DailyWithWeekends,
                    shareableUrl = "https://clausr.dk",
                    historicAlbums = listOf(
                        HistoricAlbum(
                            album = Album(
                                artist = "Black Sabbath",
                                artistOrigin = "UK",
                                name = "Paranoid",
                                slug = "paranoid",
                                releaseDate = "1970",
                                globalReviewsUrl = "https://1001albumsgenerator.com/albums/7DBES3oV6jjAmWob7kJg6P/paranoid",
                                wikipediaUrl = "https://en.wikipedia.org/wiki/Paranoid_(album)",
                                spotifyId = "7DBES3oV6jjAmWob7kJg6P",
                                appleMusicId = "785232473",
                                tidalId = 34450059,
                                amazonMusicId = "B073JYN27B",
                                youtubeMusicId = "OLAK5uy_l-gXxtv23EojUteRu5Zq1rKW3InI_bwsU",
                                genres = emptyList(),
                                subGenres = emptyList(),
                                imageUrl = "https://i.scdn.co/image/ab2eae28bb2a55667ee727711aeccc7f37498414",
                                qobuzId = null,
                                deezerId = null,
                            ),
                            rating = Rating.Rated(5),
                            review = "Fed",
                            generatedAt = Instant.now(),
                            globalRating = 5.0,
                            isRevealed = true,
                        ),
                    ),
                ),
                currentAlbum = Album(
                    artist = "Black Sabbath",
                    artistOrigin = "UK",
                    name = "Paranoid",
                    slug = "paranoid",
                    releaseDate = "1970",
                    globalReviewsUrl = "https://1001albumsgenerator.com/albums/7DBES3oV6jjAmWob7kJg6P/paranoid",
                    wikipediaUrl = "https://en.wikipedia.org/wiki/Paranoid_(album)",
                    spotifyId = "7DBES3oV6jjAmWob7kJg6P",
                    appleMusicId = "785232473",
                    tidalId = 34450059,
                    amazonMusicId = "B073JYN27B",
                    youtubeMusicId = "OLAK5uy_l-gXxtv23EojUteRu5Zq1rKW3InI_bwsU",
                    genres = emptyList(),
                    subGenres = emptyList(),
                    imageUrl = "https://i.scdn.co/image/ab2eae28bb2a55667ee727711aeccc7f37498414",
                    qobuzId = null,
                    deezerId = null,
                ),
            )
        )
    }
}
