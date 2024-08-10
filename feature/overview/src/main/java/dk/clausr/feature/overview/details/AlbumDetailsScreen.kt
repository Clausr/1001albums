@file:OptIn(ExperimentalSharedTransitionApi::class)

package dk.clausr.feature.overview.details

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import dk.clausr.core.model.HistoricAlbum

@Composable
fun AlbumDetailsRoute(
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    modifier: Modifier = Modifier,
    viewModel: AlbumDetailsViewModel = hiltViewModel(),
) {
    val state by viewModel.album.collectAsStateWithLifecycle()

    when (val internalState = state) {
        AlbumDetailsViewModel.AlbumDetailsViewState.Loading -> CircularProgressIndicator()
        is AlbumDetailsViewModel.AlbumDetailsViewState.Success -> {
            AlbumDetailsScreen(
                modifier = modifier,
                historicAlbum = internalState.album,
                sharedTransitionScope = sharedTransitionScope,
                animatedContentScope = animatedContentScope,
            )
        }
    }
}

@Composable
fun AlbumDetailsScreen(
    historicAlbum: HistoricAlbum,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    modifier: Modifier = Modifier,
) {
    with(sharedTransitionScope) {
        Scaffold(
            modifier = modifier
                .sharedBounds(
                    sharedContentState = rememberSharedContentState(key = "bounds-${historicAlbum.album.slug}"),
                    animatedVisibilityScope = animatedContentScope,
                    resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds,
                ),
            contentWindowInsets = WindowInsets.statusBars,
            containerColor = MaterialTheme.colorScheme.background,
        ) { paddingValues ->
            Column(modifier = Modifier.padding(paddingValues)) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(historicAlbum.album.imageUrl)
                        .crossfade(true)
                        .placeholderMemoryCacheKey(historicAlbum.album.slug)
                        .memoryCacheKey(historicAlbum.album.slug)
                        .build(),
                    contentDescription = "Album cover",
                    modifier = Modifier
                        .fillMaxWidth()
                        .sharedElement(
                            state = sharedTransitionScope.rememberSharedContentState(key = "cover-${historicAlbum.album.slug}"),
                            animatedVisibilityScope = animatedContentScope,
                        ),
                    contentScale = ContentScale.FillWidth,
                )

                Text(
                    modifier = Modifier
                        .sharedBounds(
                            sharedContentState = rememberSharedContentState(key = "title-${historicAlbum.album.slug}"),
                            animatedVisibilityScope = animatedContentScope,
                            renderInOverlayDuringTransition = false,
                        )
                        .skipToLookaheadSize()
                        .fillMaxWidth(),
                    text = historicAlbum.album.name,
                    style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.SemiBold),
                )

                Text(
                    modifier = Modifier
                        .sharedBounds(
                            sharedContentState = rememberSharedContentState(key = "artist-${historicAlbum.album.slug}"),
                            animatedVisibilityScope = animatedContentScope,
                            renderInOverlayDuringTransition = false,

                            )
                        .skipToLookaheadSize()
                        .fillMaxWidth(),
                    text = historicAlbum.album.artist,
                    style = MaterialTheme.typography.titleLarge,
                )
            }
        }
    }
}