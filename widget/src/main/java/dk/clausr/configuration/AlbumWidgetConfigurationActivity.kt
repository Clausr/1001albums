package dk.clausr.configuration

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.glance.appwidget.updateAll
import coil.compose.AsyncImage
import dagger.hilt.android.AndroidEntryPoint
import dk.clausr.core.data_widget.SerializedWidgetState.Error
import dk.clausr.core.data_widget.SerializedWidgetState.Loading
import dk.clausr.core.data_widget.SerializedWidgetState.NotInitialized
import dk.clausr.core.data_widget.SerializedWidgetState.Success
import dk.clausr.widget.SimplifiedAlbumWidget
import dk.clausr.worker.SimplifiedWidgetWorker
import kotlinx.coroutines.launch

@AndroidEntryPoint
@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
class AlbumWidgetConfigurationActivity : ComponentActivity() {
    private val appWidgetId: Int by lazy {
        intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID
    }

    private val vm: ConfigurationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setResult(Activity.RESULT_CANCELED)

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

//            LaunchedEffect(widgetState) {
////                if (widgetState is Success) {
////                    val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
////                    setResult(Activity.RESULT_OK, resultValue)
////                }
//
//            }

            var projectId by remember(widgetState) {
                mutableStateOf(
                    widgetState.projectId ?: ""
                )
            }

            val keyboardController = LocalSoftwareKeyboardController.current
            val scope = rememberCoroutineScope()
            Scaffold(contentWindowInsets = WindowInsets(0, 0, 0, 0), topBar = {
                TopAppBar(title = { Text("1001 albums") })
            }) { padding ->
                Column(
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(padding)
                        .padding(horizontal = 16.dp)
                        .background(MaterialTheme.colorScheme.background),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    TextField(modifier = Modifier.fillMaxWidth(),
                        label = { Text("Project name (username)") },
                        singleLine = true,
                        value = projectId,
                        onValueChange = { projectId = it })

                    Button(
                        onClick = {
                            if (projectId.isNotBlank()) {
                                vm.setProjectId(projectId)
                            }

                            scope.launch {
                                keyboardController?.hide()
                            }

                        }, enabled = projectId.isNotBlank() && !projectId.equals(
                            widgetState.projectId, ignoreCase = true
                        )
                    ) {
                        Text("Click to set project")
                    }

                    when (val state = widgetState) {
                        is Error -> Text("Error :( ${state.message}")
                        is Loading -> CircularProgressIndicator()
                        NotInitialized -> Text("Project not initialized")
                        is Success -> {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    modifier = Modifier.fillMaxWidth(),
                                    text = "Todays album:",
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

                                        contentDescription = "Current Album"
                                    )
                                    if (state.data.newAvailable) {
                                        Text(
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.fillMaxSize(),
                                            text = "New album available.\n\nRate this album to get the latest cover",
                                            style = MaterialTheme.typography.labelLarge.copy(
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                }
                            }

                            Button(onClick = {
                                val resultValue = Intent().putExtra(
                                    AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId
                                )
                                setResult(Activity.RESULT_OK, resultValue)

                                scope.launch {
                                    SimplifiedAlbumWidget.updateAll(applicationContext)
                                }
                                SimplifiedWidgetWorker.start(this@AlbumWidgetConfigurationActivity)

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
}
