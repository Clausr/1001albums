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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dk.clausr.a1001albumsgenerator.analytics.LocalAnalyticsHelper
import dk.clausr.a1001albumsgenerator.ui.components.AlbumCover
import dk.clausr.a1001albumsgenerator.ui.extensions.logClickEvent
import dk.clausr.a1001albumsgenerator.ui.extensions.logRatingGiven
import dk.clausr.a1001albumsgenerator.ui.helper.displayName
import dk.clausr.a1001albumsgenerator.ui.theme.OagTheme
import dk.clausr.core.common.extensions.openProject
import dk.clausr.core.data_widget.SerializedWidgetState
import dk.clausr.core.data_widget.SerializedWidgetState.Companion.projectUrl
import dk.clausr.core.model.Album
import dk.clausr.core.model.StreamingPlatform
import dk.clausr.core.model.StreamingService
import dk.clausr.feature.overview.preview.albumPreviewData
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
    val analyticsHelper = LocalAnalyticsHelper.current
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
                    analyticsHelper.logRatingGiven(gaveRating = stars != null)
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
    val analyticsHelper = LocalAnalyticsHelper.current

    Column(modifier = modifier) {
        Box(
            modifier = Modifier.aspectRatio(1f),
            contentAlignment = Alignment.Center,
        ) {
            AlbumCover(
                coverUrl = album.imageUrl,
                albumSlug = album.slug,
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
                            Text(text = stringResource(R.string.rating_did_not_listen))
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
                onClick = {
                    analyticsHelper.logClickEvent("Wikipedia")
                    openLink(album.wikipediaUrl)
                },
            ) {
                Icon(
                    painterResource(id = uiR.drawable.ic_wiki),
                    contentDescription = "Open albums wikipedia page",
                )
            }

            streamingService.takeIf { it?.streamingLink?.isNotBlank() == true }?.let { streaming ->
                FilledIconButton(
                    onClick = {
                        analyticsHelper.logClickEvent("Open streaming service")
                        openLink(streaming.streamingLink)
                    },
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play album on ${streaming.platform.displayName()}",
                    )
                }
            }

            FilledIconButton(
                onClick = {
                    analyticsHelper.logClickEvent("Open project website")
                    openProject()
                },
            ) {
                Icon(
                    painterResource(id = uiR.drawable.ic_open_external),
                    contentDescription = "Open project website",
                )
            }
        }
    }
}

@Preview
@Composable
private fun NewAlbumAvailablePreview() {
    OagTheme {
        BigCurrentAlbum(
            newAlbumAvailable = true,
            streamingService = StreamingService("id", StreamingPlatform.Spotify),
            openProject = {},
            openLink = {},
            onRating = {},
            album = albumPreviewData("black-sabbath"),
        )
    }
}

@Preview
@Composable
private fun CurrentAlbumPreview() {
    OagTheme {
        BigCurrentAlbum(
            newAlbumAvailable = false,
            streamingService = StreamingService("id", StreamingPlatform.Spotify),
            openProject = {},
            openLink = {},
            onRating = {},
            album = albumPreviewData("black-sabbath"),
        )
    }
}

