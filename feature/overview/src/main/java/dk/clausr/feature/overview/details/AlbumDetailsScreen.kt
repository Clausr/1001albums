package dk.clausr.feature.overview.details

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import dk.clausr.a1001albumsgenerator.ui.components.LocalNavAnimatedVisibilityScope
import dk.clausr.a1001albumsgenerator.ui.components.LocalSharedTransitionScope
import dk.clausr.a1001albumsgenerator.ui.theme.OagTheme
import dk.clausr.core.common.android.openLink
import dk.clausr.core.model.HistoricAlbum
import dk.clausr.core.model.StreamingPlatform
import dk.clausr.core.model.StreamingServices
import dk.clausr.feature.overview.preview.historicAlbumPreviewData

@Composable
fun AlbumDetailsRoute(
    modifier: Modifier = Modifier,
    viewModel: AlbumDetailsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    when (val internalState = state) {
        AlbumDetailsViewModel.AlbumDetailsViewState.Loading -> CircularProgressIndicator()
        is AlbumDetailsViewModel.AlbumDetailsViewState.Success -> {
            AlbumDetailsScreen(
                modifier = modifier,
                historicAlbum = internalState.album,
                streamingPlatform = internalState.streamingPlatform,
            )
        }
    }
}

@Composable
fun AlbumDetailsScreen(
    historicAlbum: HistoricAlbum,
    streamingPlatform: StreamingPlatform,
    modifier: Modifier = Modifier,
) {
    val animatedContentScope = LocalNavAnimatedVisibilityScope.current
    with(LocalSharedTransitionScope.current) {
        Scaffold(
            modifier = modifier
                .sharedBounds(
                    sharedContentState = rememberSharedContentState(key = "bounds-${historicAlbum.album.slug}"),
                    animatedVisibilityScope = animatedContentScope,
                    resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds,
                ),
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            containerColor = MaterialTheme.colorScheme.background,
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(paddingValues),
            ) {
                AsyncImage(
                    model = historicAlbum.album.imageUrl,
                    contentDescription = "Album cover",
                    modifier = Modifier
                        .fillMaxWidth()
                        .sharedElement(
                            state = rememberSharedContentState(key = "cover-${historicAlbum.album.slug}"),
                            animatedVisibilityScope = animatedContentScope,
                        ),
                    contentScale = ContentScale.FillWidth,
                )

                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .sharedBounds(
                            sharedContentState = rememberSharedContentState(key = "title-${historicAlbum.album.slug}"),
                            animatedVisibilityScope = animatedContentScope,
                        ),
                    text = historicAlbum.album.name,
                    style = MaterialTheme.typography.displaySmall,
                    textAlign = TextAlign.Center,
                )

                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .sharedBounds(
                            sharedContentState = rememberSharedContentState(key = "artist-${historicAlbum.album.slug}"),
                            animatedVisibilityScope = animatedContentScope,
                        ),
                    text = historicAlbum.album.artist,
                    textAlign = TextAlign.Center,
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, alignment = Alignment.CenterHorizontally),
                ) {
                    val context = LocalContext.current
                    StreamingServices.from(historicAlbum.album).getStreamingLinkFor(streamingPlatform)?.let { streamingLink ->
                        FilledTonalButton(
                            modifier = Modifier,
                            onClick = {
                                context.openLink(streamingLink)
                            },
                        ) {
                            Icon(
                                modifier = Modifier.padding(end = 8.dp),
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Play",
                            )
                            Text(text = "Play")
                        }
                    }

                    FilledTonalButton(
                        onClick = {
                            context.openLink(historicAlbum.album.wikipediaUrl)
                        },
                    ) {
                        Icon(
                            modifier = Modifier.padding(end = 8.dp),
                            painter = painterResource(id = dk.clausr.a1001albumsgenerator.ui.R.drawable.ic_wiki),
                            contentDescription = "Wikipedia"
                        )
                        Text(text = "Wikipedia")
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun DetailsPreview() {
    OagTheme {
        AnimatedVisibility(visible = true) {
            SharedTransitionScope {
                CompositionLocalProvider(
                    LocalNavAnimatedVisibilityScope provides this@AnimatedVisibility,
                    LocalSharedTransitionScope provides this
                ) {
                    AlbumDetailsScreen(historicAlbum = historicAlbumPreviewData(), streamingPlatform = StreamingPlatform.Tidal)
                }
            }
        }
    }
}