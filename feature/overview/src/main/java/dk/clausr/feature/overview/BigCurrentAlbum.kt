package dk.clausr.feature.overview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import dk.clausr.a1001albumsgenerator.ui.helper.icon
import dk.clausr.a1001albumsgenerator.ui.theme.OagTheme
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
    modifier: Modifier = Modifier,
) {
    when (state) {
        is SerializedWidgetState.Error -> {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.errorContainer),
            )
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
            BigCurrentAlbum(
                modifier = modifier,
                album = album,
                shouldBeRated = state.data.newAvailable,
                openProject = { state.projectUrl?.let(openLink) },
                openLink = openLink,
                streamingService = state.data.streamingServices.services.firstOrNull { it.platform == state.data.preferredStreamingPlatform },
            )
        }
    }
}

@Composable
fun BigCurrentAlbum(
    album: Album,
    shouldBeRated: Boolean,
    openProject: () -> Unit,
    openLink: (url: String) -> Unit,
    streamingService: StreamingService?,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Box(
            modifier = Modifier.aspectRatio(1f),
            contentAlignment = Alignment.Center,
        ) {
            AsyncImage(
                modifier = Modifier
                    .fillMaxWidth()
                    .blur(if (shouldBeRated) 8.dp else 0.dp),
                contentScale = ContentScale.FillWidth,
                model = album.imageUrl,
                contentDescription = "Cover",
            )
        }

        Text(
            modifier = Modifier.fillMaxWidth(),
            text = album.name,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.displaySmall,
        )
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = album.artist,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = album.releaseDate,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(
                space = 16.dp,
                alignment = Alignment.CenterHorizontally,
            ),
        ) {
            streamingService.takeIf { it?.streamingLink?.isNotBlank() == true }?.let { streaming ->
                FilledIconButton(
                    onClick = { openLink(streaming.streamingLink) },
                ) {
                    Icon(
                        painter = painterResource(id = streaming.platform.icon()),
                        contentDescription = null,
                    )
                }
            }
            FilledIconButton(
                onClick = { openLink(album.wikipediaUrl) },
            ) {
                Icon(
                    painterResource(id = uiR.drawable.ic_wiki),
                    contentDescription = null,
                )
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
            shouldBeRated = false,
            streamingService = StreamingService("id", StreamingPlatform.Spotify),
            openProject = {},
            openLink = {},
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
