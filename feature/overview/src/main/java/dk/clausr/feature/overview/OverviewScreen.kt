package dk.clausr.feature.overview

import androidx.compose.foundation.layout.Column
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
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Project name:", style = MaterialTheme.typography.labelLarge)
                            Text(state.project.name)
                        }
                    }
                }

                items(state.albums) { album ->
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(text = "${album.artist} - ${album.name}")

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
