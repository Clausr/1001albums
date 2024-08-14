package dk.clausr.feature.overview.details

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import dk.clausr.a1001albumsgenerator.ui.components.LocalNavAnimatedVisibilityScope
import dk.clausr.a1001albumsgenerator.ui.components.LocalSharedTransitionScope
import dk.clausr.a1001albumsgenerator.ui.theme.OagTheme
import dk.clausr.core.model.HistoricAlbum
import dk.clausr.feature.overview.preview.historicAlbumPreviewData

@Composable
fun AlbumDetailsRoute(
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
            )
        }
    }
}

@Composable
fun AlbumDetailsScreen(
    historicAlbum: HistoricAlbum,
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
                    AlbumDetailsScreen(historicAlbum = historicAlbumPreviewData())
                }
            }
        }
    }
}