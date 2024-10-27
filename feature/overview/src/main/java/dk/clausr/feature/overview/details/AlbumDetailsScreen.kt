package dk.clausr.feature.overview.details

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import dk.clausr.a1001albumsgenerator.ui.components.LocalNavAnimatedVisibilityScope
import dk.clausr.a1001albumsgenerator.ui.components.LocalSharedTransitionScope
import dk.clausr.a1001albumsgenerator.ui.theme.OagTheme
import dk.clausr.core.common.android.openLink
import dk.clausr.core.model.Rating
import dk.clausr.core.model.StreamingPlatform
import dk.clausr.core.model.StreamingServices
import dk.clausr.feature.overview.preview.historicAlbumPreviewData

@Composable
fun AlbumDetailsRoute(
    modifier: Modifier = Modifier,
    viewModel: AlbumDetailsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    AlbumDetailsScreen(
        modifier = modifier,
        state = state,
        listName = viewModel.listName ?: "nozhing",
    )
}

@Composable
fun AlbumDetailsScreen(
    state: AlbumDetailsViewModel.AlbumDetailsViewState,
    modifier: Modifier = Modifier,
    listName: String = "List",
) {
    val historicAlbum = state.album
    val animatedContentScope = LocalNavAnimatedVisibilityScope.current
    with(LocalSharedTransitionScope.current) {
        Scaffold(
            modifier = modifier
                .sharedBounds(
                    sharedContentState = rememberSharedContentState(key = "$listName-bounds-${historicAlbum?.album?.slug}"),
                    animatedVisibilityScope = animatedContentScope,
                ),
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            containerColor = MaterialTheme.colorScheme.background,
        ) { paddingValues ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding()
                    .padding(paddingValues),
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(historicAlbum?.album?.imageUrl)
                        .crossfade(true)
                        .placeholderMemoryCacheKey(historicAlbum?.album?.slug)
                        .memoryCacheKey(historicAlbum?.album?.slug)
                        .build(),
                    contentDescription = "Album cover",
                    modifier = Modifier
                        .fillMaxWidth()
                        .sharedElement(
                            state = rememberSharedContentState(key = "$listName-cover-${historicAlbum?.album?.slug}"),
                            animatedVisibilityScope = animatedContentScope,
                        ),
                    contentScale = ContentScale.FillWidth,
                )

                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .sharedBounds(
                            sharedContentState = rememberSharedContentState(key = "$listName-title-${historicAlbum?.album?.slug}"),
                            animatedVisibilityScope = animatedContentScope,
                            resizeMode = SharedTransitionScope.ResizeMode.ScaleToBounds(
                                contentScale = ContentScale.FillWidth,
                                alignment = Alignment.CenterStart,
                            ),
                        ),
                    text = historicAlbum?.album?.name.orEmpty(),
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                )

                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .sharedBounds(
                            sharedContentState = rememberSharedContentState(key = "$listName-artist-${historicAlbum?.album?.slug}"),
                            animatedVisibilityScope = animatedContentScope,
                            resizeMode = SharedTransitionScope.ResizeMode.ScaleToBounds(
                                contentScale = ContentScale.FillWidth,
                                alignment = Alignment.CenterStart,
                            ),
                        ),
                    text = historicAlbum?.album?.artist.orEmpty(),
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                )

                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .sharedBounds(
                            sharedContentState = rememberSharedContentState(key = "$listName-date-${historicAlbum?.album?.slug}"),
                            animatedVisibilityScope = animatedContentScope,
                        )
                        .padding(bottom = 16.dp),
                    text = historicAlbum?.album?.releaseDate.orEmpty(),
                    textAlign = TextAlign.Center,
                )

                if (historicAlbum?.album != null) {
                    Row(
                        modifier = Modifier
                            .sharedBounds(
                                sharedContentState = rememberSharedContentState(key = "$listName-play-${historicAlbum.album.slug}"),
                                animatedVisibilityScope = animatedContentScope,
                                resizeMode = SharedTransitionScope.ResizeMode.ScaleToBounds(),
                            )
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, alignment = Alignment.CenterHorizontally),
                    ) {
                        val context = LocalContext.current
                        StreamingServices.from(historicAlbum.album)
                            .getStreamingLinkFor(state.streamingPlatform)
                            ?.let { streamingLink ->
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
                                contentDescription = "Wikipedia",
                            )
                            Text(text = "Wikipedia")
                        }
                    }
                }

                val ratingText = when (val rating = historicAlbum?.rating) {
                    Rating.DidNotListen -> "Did not listen"
                    is Rating.Rated -> "${rating.rating}⭐️"
                    Rating.Unrated -> "Unrated"
                    else -> ""
                }

                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                        .padding(horizontal = 16.dp),
                    text = ratingText,
                    style = MaterialTheme.typography.displaySmall,
                    textAlign = TextAlign.Center,
                )

                if (historicAlbum?.review?.isNotBlank() == true) {
                    Text(
                        text = "“${historicAlbum.review}”",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .skipToLookaheadSize(),
                        style = MaterialTheme.typography.headlineSmall.copy(fontStyle = FontStyle.Italic),
                        textAlign = TextAlign.Center,
                    )
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
            SharedTransitionLayout {
                CompositionLocalProvider(
                    LocalNavAnimatedVisibilityScope provides this@AnimatedVisibility,
                    LocalSharedTransitionScope provides this,
                ) {
                    AlbumDetailsScreen(
                        state = AlbumDetailsViewModel.AlbumDetailsViewState(
                            album = historicAlbumPreviewData(),
                            streamingPlatform = StreamingPlatform.Tidal,
                        ),
                    )
                }
            }
        }
    }
}
