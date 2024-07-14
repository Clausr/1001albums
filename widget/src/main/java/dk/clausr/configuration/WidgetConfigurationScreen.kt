package dk.clausr.configuration

import androidx.compose.animation.AnimatedContent
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
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
import dk.clausr.core.model.StreamingPlatform
import dk.clausr.extensions.icon
import dk.clausr.widget.R
import kotlinx.coroutines.launch

@Composable
fun WidgetConfigurationRoute(
    onProjectIdSet: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ConfigurationViewModel = hiltViewModel(),
) {
    val widgetState by viewModel.widgetState.collectAsState()

    WidgetConfigurationScreen(
        widgetState = widgetState,
        modifier = modifier,
        onSetProjectId = {
            viewModel.setProjectId(it)
            onProjectIdSet()
        },
        onApplyChanges = {
            viewModel.updateWidgets()
        },
        selectPreferredStreamingPlatform = {
            viewModel.setPreferredStreamingPlatform(it)
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WidgetConfigurationScreen(
    widgetState: SerializedWidgetState,
    onSetProjectId: (String) -> Unit,
    onApplyChanges: () -> Unit,
    selectPreferredStreamingPlatform: (StreamingPlatform) -> Unit,
    modifier: Modifier = Modifier,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }
    var projectId by remember(widgetState) {
        mutableStateOf(
            widgetState.projectId.orEmpty(),
        )
    }

    val setProjectButtonEnabled by remember(projectId) {
        mutableStateOf(
            projectId.isNotBlank() && !projectId.equals(
                widgetState.projectId, ignoreCase = true,
            ),
        )
    }

    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.config_toolbar_title)) },
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
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                label = { Text(stringResource(id = R.string.config_project_input_label)) },
                singleLine = true,
                value = projectId,
                keyboardActions = KeyboardActions(onDone = { onSetProjectId(projectId) }),
                onValueChange = { projectId = it },
                trailingIcon = {
                    AnimatedContent(
                        targetState = setProjectButtonEnabled,
                    ) { setProjectEnabled ->
                        if (setProjectEnabled) {
                            IconButton(
                                onClick = {
                                    onSetProjectId(projectId)
                                    scope.launch {
                                        keyboardController?.hide()
                                    }
                                },
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = stringResource(
                                        id = R.string.config_set_project_button_title,
                                    ),
                                )
                            }
                        } else {
                            IconButton(
                                onClick = {
                                    projectId = ""
                                    focusRequester.requestFocus()
                                },
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = stringResource(
                                        id = R.string.config_clear_project_button_title,
                                    ),
                                )
                            }
                        }
                    }
                },
            )

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
                            contentAlignment = Alignment.Center,
                        ) {
                            AsyncImage(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .aspectRatio(1f)
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .alpha(if (state.data.newAvailable) 0.25f else 1f),
                                model = state.data.coverUrl,
                                contentDescription = stringResource(id = R.string.album_cover_a11y),
                            )
                            if (state.data.newAvailable) {
                                Text(
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxSize(),
                                    text = stringResource(id = R.string.configuration_new_album_available),
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                    ),
                                )
                            }
                        }
                    }

                    Row {
                        state.data.streamingServices.services.forEach { streamingService ->
                            val buttonColor =
                                if (state.data.preferredStreamingPlatform == streamingService.platform) {
                                    IconButtonDefaults.filledIconButtonColors()
                                } else {
                                    IconButtonDefaults.iconButtonColors()
                                }

                            IconButton(
                                onClick = { selectPreferredStreamingPlatform(streamingService.platform) },
                                colors = buttonColor,
                            ) {
                                Icon(
                                    painterResource(
                                        id = streamingService.platform.icon(),
                                    ),
                                    contentDescription = streamingService.platform.name,
                                )
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
