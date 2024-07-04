package dk.clausr.feature.overview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import dk.clausr.core.model.Album
import dk.clausr.core.model.HistoricAlbum
import dk.clausr.core.model.Project
import dk.clausr.core.model.Rating
import dk.clausr.core.model.UpdateFrequency
import java.time.Instant

@Composable
fun OverviewRoute(
    modifier: Modifier = Modifier,
    widgetView: @Composable () -> Unit = {},
    onConfigureWidget: () -> Unit,
    viewModel: OverviewViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    OverviewScreen(
        widgetView = widgetView,
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
    widgetView: @Composable () -> Unit = {},
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(title = { Text(text = "1001 albums") },
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

        Box(modifier = Modifier.padding(innerPadding)) {
            when (state) {
                OverviewUiState.Error -> Text("Error")
                OverviewUiState.Loading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }

                is OverviewUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                    ) {
                        item {
                            widgetView()
                        }
                        if (state.albums.isNotEmpty()) {
                            item {
                                Text(
                                    text = stringResource(
                                        id = R.string.history_header,
                                        state.albums.count { it.rating is Rating.Rated },
                                        state.albums.size
                                    ),
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                )
                            }
                        }
                        items(state.albums, key = { it.generatedAt }) { historicAlbum ->
                            HistoricAlbumCard(historicAlbum)
                        }
                    }
                }
            }
        }
    }


}

@Composable
private fun HistoricAlbumCard(historicAlbum: HistoricAlbum) {
    val album = historicAlbum.album
    Card(
        shape = RoundedCornerShape(
            topStart = 0.dp,
            bottomStart = 0.dp,
            bottomEnd = 8.dp,
            topEnd = 8.dp
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AsyncImage(
                modifier = Modifier
                    .width(60.dp)
                    .shadow(elevation = 4.dp),
                model = album.imageUrl,
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
                    style = MaterialTheme.typography.labelLarge
                )
                Text(text = album.name)
            }
            val rating = when (val rating = historicAlbum.rating) {
                Rating.DidNotListen -> "DNL"
                is Rating.Rated -> rating.rating.toString()
                Rating.Unrated -> "Unrated"
            }
            Text(
                modifier = Modifier.padding(end = 16.dp),
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
                    shareableUrl = "https://clausr.dk"
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
                albums = listOf(
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
                        globalRating = 5.0
                    ),
                )
            )
        )
    }
}
