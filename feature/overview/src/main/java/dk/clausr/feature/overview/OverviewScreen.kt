package dk.clausr.feature.overview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import timber.log.Timber

@Composable
fun OverviewRoute(
        viewModel: OverviewViewModel = hiltViewModel(),
) {
    val projectId by viewModel.projectId.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Timber.d("ProjectId: $projectId, state: $uiState")

    OverviewScreen(
            state = uiState,
    )
}

@Composable
internal fun OverviewScreen(
        state: OverviewUiState,
        modifier: Modifier = Modifier
) {

    when (state) {
        OverviewUiState.Error -> Text("Error")
        OverviewUiState.Loading -> CircularProgressIndicator()
        is OverviewUiState.Success -> {
            LazyColumn(modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)) {
                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("Project name:", style = MaterialTheme.typography.labelLarge)
                        Text(state.project.name)
                    }
                }

                items(state.albums) { album ->
                    Card() {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            AsyncImage(model = album.images.firstOrNull(), contentDescription = "cover")
                            Column(modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp, horizontal = 8.dp)) {
                                Text(text = album.artist, style = MaterialTheme.typography.labelLarge)
                                Text(text = album.name)
                            }
                        }
                    }
                }
            }
        }
    }
}


@Preview
@Composable
private fun OverviewPreview() {
    OverviewScreen(
            state = OverviewUiState.Loading
    )
}
