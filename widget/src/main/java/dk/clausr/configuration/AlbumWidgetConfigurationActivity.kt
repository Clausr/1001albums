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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.work.WorkManager
import coil.compose.AsyncImage
import dagger.hilt.android.AndroidEntryPoint
import dk.clausr.worker.UpdateWidgetWorker
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
class AlbumWidgetConfigurationActivity : ComponentActivity() {

    private val manager: GlanceAppWidgetManager by lazy {
        GlanceAppWidgetManager(this)
    }

    private val appWidgetId: Int by lazy {
        intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID
    }

    private val vm: ConfigurationViewModel by viewModels()

    override fun onResume() {
        super.onResume()
        updateView()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Timber.i("Widget ID: $appWidgetId")
        val glanceId = manager.getGlanceIdBy(appWidgetId)


//        vm.setWidgetId(appWidgetId)
        setResult(Activity.RESULT_CANCELED)

        updateView()

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }
    }

    private fun updateView() {
        Timber.i("Update view")


        setContent {
            val widget by vm.widget.collectAsState()

            Timber.d("Project: -- widget: $widget")

            var projectId by remember(widget?.projectName) { mutableStateOf(widget?.projectName ?: "") }
            val context = LocalContext.current
            val coroutineScope = rememberCoroutineScope()

            fun closeConfiguration() {
                Timber.d("Close configuration")
                val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)

                WorkManager.getInstance(this)
                    .enqueue(UpdateWidgetWorker.refreshAlbumRepeatedly(projectId = "decoid", widgetId = appWidgetId))
//                    .enqueue(UpdateWidgetWorker.doSomething(projectId = "decoid", widgetId = appWidgetId))

                setResult(Activity.RESULT_OK, resultValue)
                finish()
            }

            val keyboardController = LocalSoftwareKeyboardController.current
            val scope = rememberCoroutineScope()
            Scaffold(
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                topBar = {
                    TopAppBar(title = { Text("1001 albums") })
                }
            ) { padding ->
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
                    TextField(
                        modifier = Modifier
                            .fillMaxWidth(),
                        label = { Text("Project name (username)") },
                        singleLine = true,
                        value = projectId,
                        onValueChange = { projectId = it })

                    Button(
                        onClick = {
                            if (projectId.isNotBlank()) {
                                vm.setProjectId(projectId)
                                Timber.d("Project id: $projectId")
                            }

                            scope.launch {
                                keyboardController?.hide()
                            }

                        },
                        enabled = projectId.isNotBlank() && !projectId.equals(widget?.projectName, ignoreCase = true)
                    ) {
                        Text("Click to set project")
                    }

                    if (widget != null) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            AsyncImage(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                model = widget?.currentCoverUrl, contentDescription = "Current Album"
                            )
                            Text("${widget?.currentAlbumArtist} - ${widget?.currentAlbumTitle}")
                        }

                        Button(onClick = ::closeConfiguration) {
                            Text("Apply changes")
                        }
                    }
                }
            }
        }
    }
}
