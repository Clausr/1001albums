package dk.clausr.feature.overview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.twotone.Star
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import dk.clausr.a1001albumsgenerator.ui.theme.OagTheme
import dk.clausr.core.common.extensions.openProject
import dk.clausr.core.data_widget.SerializedWidgetState
import dk.clausr.core.data_widget.SerializedWidgetState.Companion.projectUrl
import dk.clausr.core.model.Album
import dk.clausr.core.model.StreamingPlatform
import dk.clausr.core.model.StreamingService
import dk.clausr.a1001albumsgenerator.ui.R as uiR

// TODO State and album should be together...
@Composable
fun BigCurrentAlbum(
    state: SerializedWidgetState,
    album: Album,
    openLink: (url: String) -> Unit,
    startBurstUpdate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (state) {
        is SerializedWidgetState.Error -> {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.errorContainer),
                contentAlignment = Alignment.Center,
            ) {
                Text(modifier = Modifier.fillMaxWidth(), text = state.message)
            }
        }

        is SerializedWidgetState.Loading -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        }

        SerializedWidgetState.NotInitialized -> {}
        is SerializedWidgetState.Success -> {
            val context = LocalContext.current
            val streamingService = state.data.streamingServices.services.firstOrNull { it.platform == state.data.preferredStreamingPlatform }
            BigCurrentAlbum(
                modifier = modifier,
                album = album,
                newAlbumAvailable = state.data.newAvailable,
                openProject = { state.projectUrl?.let(openLink) },
                openLink = openLink,
                streamingService = streamingService,
                onRating = { stars ->
                    context.openProject(state.currentProjectId, stars)
                    startBurstUpdate()
                },
            )
        }
    }
}

@Composable
fun BigCurrentAlbum(
    album: Album,
    newAlbumAvailable: Boolean,
    openProject: () -> Unit,
    openLink: (url: String) -> Unit,
    streamingService: StreamingService?,
    onRating: (rating: Int?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Box(
            modifier = Modifier.aspectRatio(1f),
            contentAlignment = Alignment.Center,
        ) {
            AsyncImage(
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.FillWidth,
                model = album.imageUrl,
                contentDescription = "Cover",
            )

            if (newAlbumAvailable) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background.copy(alpha = 0.75f)),
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = "How would you rate this?",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                    )
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(
                            space = 8.dp,
                            alignment = Alignment.CenterHorizontally,
                        ),
                    ) {
                        for (stars in 1..5) {
                            val icon = if (stars == 1) Icons.Default.Star else Icons.TwoTone.Star

                            IconButton(onClick = { onRating(stars) }) {
                                Icon(imageVector = icon, contentDescription = "Rate $stars")
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Button(onClick = { onRating(null) }) {
                            Text(text = "Did not listen")
                        }
                    }
                }
            }
        }

        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            text = album.name,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = album.artist,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleLarge,
        )
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = album.releaseDate,
            textAlign = TextAlign.Center,
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(
                space = 16.dp,
                alignment = Alignment.CenterHorizontally,
            ),
        ) {
            FilledIconButton(
                onClick = { openLink(album.wikipediaUrl) },
            ) {
                Icon(
                    painterResource(id = uiR.drawable.ic_wiki),
                    contentDescription = null,
                )
            }

            streamingService.takeIf { it?.streamingLink?.isNotBlank() == true }?.let { streaming ->
                FilledIconButton(
                    onClick = { openLink(streaming.streamingLink) },
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                    )
                }
            }

            FilledIconButton(
                onClick = openProject,
            ) {
                Icon(
                    painterResource(id = uiR.drawable.ic_open_external),
                    contentDescription = null,
                )
            }
        }
    }
}

@Preview
@Composable
private fun CurrentAlbumPreview() {
    OagTheme {
        BigCurrentAlbum(
            newAlbumAvailable = true,
            streamingService = StreamingService("id", StreamingPlatform.Spotify),
            openProject = {},
            openLink = {},
            onRating = {},
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
        )
    }
}
