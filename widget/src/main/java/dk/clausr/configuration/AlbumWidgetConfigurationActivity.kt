package dk.clausr.configuration

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.glance.appwidget.updateAll
import coil.compose.AsyncImage
import dagger.hilt.android.AndroidEntryPoint
import dk.clausr.core.data_widget.SerializedWidgetState.Companion.projectId
import dk.clausr.core.data_widget.SerializedWidgetState.Error
import dk.clausr.core.data_widget.SerializedWidgetState.Loading
import dk.clausr.core.data_widget.SerializedWidgetState.NotInitialized
import dk.clausr.core.data_widget.SerializedWidgetState.Success
import dk.clausr.widget.AlbumCoverWidget2
import dk.clausr.widget.R
import kotlinx.coroutines.launch

@AndroidEntryPoint
@OptIn(ExperimentalMaterial3Api::class)
class AlbumWidgetConfigurationActivity : ComponentActivity() {
    private val appWidgetId: Int by lazy {
        intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID
    }

    private val vm: ConfigurationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setResult(RESULT_CANCELED)

        updateView()

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }
    }

    private fun updateView() {
        setContent {
            val widgetState by vm.widgetState.collectAsState()

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
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                topBar = {
                    TopAppBar(
                        title = { Text(stringResource(id = R.string.config_toolbar_title)) },
                        navigationIcon = {
                            IconButton(onClick = { finish() }) {
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
                                vm.setProjectId(projectId)

                                val resultValue = Intent().putExtra(
                                    AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId
                                )
                                setResult(RESULT_OK, resultValue)
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

                            Button(onClick = {
                                scope.launch {
                                    AlbumCoverWidget2().updateAll(this@AlbumWidgetConfigurationActivity)
                                }
                                finish()
                            }) {
                                Text("Apply changes")
                            }
                        }
                    }
                }
            }
        }
    }

    override fun finish() {
        vm.finish()
        super.finish()
    }
}
