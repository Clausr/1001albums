package dk.clausr.feature.overview.details

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import dk.clausr.core.model.HistoricAlbum

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
    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            AsyncImage(
                model = historicAlbum.album.imageUrl,
                contentDescription = "Album cover",
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.FillWidth,
            )

            Text(
                modifier = Modifier.fillMaxWidth(),
                text = historicAlbum.album.artist
            )
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = historicAlbum.album.name
            )
        }
    }
}