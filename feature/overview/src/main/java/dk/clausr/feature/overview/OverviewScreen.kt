package dk.clausr.feature.overview

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun OverviewRoute(
    viewModel: OverviewViewModel = hiltViewModel(),
) {
    val projectId by viewModel.project.collectAsStateWithLifecycle()
    val groupId by viewModel.group.collectAsStateWithLifecycle()

    OverviewScreen(
        projectId = projectId,
        groupId = groupId
    )
}

@Composable
internal fun OverviewScreen(
    projectId: String?,
    groupId: String?,
    modifier: Modifier = Modifier
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text("ProjectId", style = MaterialTheme.typography.labelLarge)
        Text(projectId.toString())
        Text("GroupId", style = MaterialTheme.typography.labelLarge)
        Text(groupId.toString())
    }
}


@Preview
@Composable
private fun OverviewPreview() {
    OverviewScreen(
        projectId = null,
        groupId = null
    )
}
