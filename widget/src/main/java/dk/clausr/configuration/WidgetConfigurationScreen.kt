package dk.clausr.configuration

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import dk.clausr.core.data_widget.SerializedWidgetState
import dk.clausr.core.data_widget.SerializedWidgetState.Companion.projectId
import dk.clausr.core.data_widget.SerializedWidgetState.Error
import dk.clausr.core.data_widget.SerializedWidgetState.Loading
import dk.clausr.core.data_widget.SerializedWidgetState.NotInitialized
import dk.clausr.core.data_widget.SerializedWidgetState.Success
import dk.clausr.widget.R
import kotlinx.coroutines.launch

@Composable
fun WidgetConfigurationRoute(
    modifier: Modifier = Modifier,
    onUpClicked: () -> Unit,
    onProjectIdSet: () -> Unit,
    onApplyChanges: () -> Unit,
    viewModel: ConfigurationViewModel = hiltViewModel()
) {
    val widgetState by viewModel.widgetState.collectAsState()

    WidgetConfigurationScreen(
        widgetState = widgetState,
        modifier = modifier,
        onSetProjectId = {
            viewModel.setProjectId(it)
            onProjectIdSet()
        },
        onUpClicked = onUpClicked,
        onApplyChanges = {
            viewModel.updateWidgets()
            onApplyChanges()
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WidgetConfigurationScreen(
    widgetState: SerializedWidgetState,
    onUpClicked: () -> Unit,
    onSetProjectId: (String) -> Unit,
    onApplyChanges: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var projectId by remember(widgetState) {
        mutableStateOf(
            widgetState.projectId.orEmpty()
        )
    }

    val setProjectButtonEnabled by remember(projectId) {
        mutableStateOf(
            projectId.isNotBlank() && !projectId.equals(
                widgetState.projectId, ignoreCase = true
            )
        )
    }
    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()
    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.config_toolbar_title)) },
                navigationIcon = {
                    IconButton(onClick = onUpClicked) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close"
                        )
                    }
                }
            )
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(16.dp)
                .background(MaterialTheme.colorScheme.background),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            TextField(
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(id = R.string.config_project_input_label)) },
                singleLine = true,
                value = projectId,
                onValueChange = { projectId = it })

            Button(
                onClick = {
                    if (projectId.isNotBlank()) {
                        onSetProjectId(projectId)
                    }

                    scope.launch {
                        keyboardController?.hide()
                    }

                },
                enabled = setProjectButtonEnabled
            ) {
                val buttonTextRes = if (setProjectButtonEnabled) {
                    R.string.config_set_project_button_title
                } else {
                    R.string.config_set_project_done_button_title
                }

                Text(stringResource(id = buttonTextRes))
            }

            when (val state = widgetState) {
                is Error -> Text("Error :( ${state.message}")
                is Loading -> CircularProgressIndicator()
                NotInitialized -> stringResource(id = R.string.configuration_state_not_initialized)
                is Success -> {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = stringResource(id = R.string.configuration_todays_album_title),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.titleMedium,
                        )

                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {

                            AsyncImage(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .aspectRatio(1f)
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .alpha(if (state.data.newAvailable) 0.25f else 1f),
                                model = state.data.coverUrl,

                                contentDescription = stringResource(id = R.string.album_cover_a11y)
                            )
                            if (state.data.newAvailable) {
                                Text(
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxSize(),
                                    text = stringResource(id = R.string.configuration_new_album_available),
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }

                    Row {

                        state.data.streamingServices.services.forEach {
                            Button(onClick = {}) {
                                Text(it.platform.name)
                            }
                        }
                    }

                    Button(onClick = {
                        onApplyChanges()
                    }) {
                        Text("Apply changes")
                    }
                }
            }
        }
    }
}