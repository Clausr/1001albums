package dk.clausr.configuration

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
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
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.updateAll
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import dagger.hilt.android.AndroidEntryPoint
import dk.clausr.widget.DailyAlbumWidget
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
class AlbumWidgetConfigurationActivity : ComponentActivity() {

//    @Inject
//    lateinit var testRepo: OagRepository

    val manager: GlanceAppWidgetManager by lazy {
        GlanceAppWidgetManager(this)
    }

    private val appWidgetId: Int by lazy {
        intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID
    }


    override fun onResume() {
        super.onResume()
//        Timber.d("On resume.. -- ${testRepo.getGroup("")}")
//        updateView()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Timber.i("Widget ID: $appWidgetId")

        setResult(Activity.RESULT_CANCELED)

        updateView()

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }


    }

    @OptIn(ExperimentalLayoutApi::class)
    private fun updateView() {
        // Discover the GlanceAppWidget
        val appWidgetManager = AppWidgetManager.getInstance(this@AlbumWidgetConfigurationActivity)
        val receivers = appWidgetManager.installedProviders
            .filter { it.provider.packageName == packageName }
            .map { it.provider.className }

        Timber.d("Receivers: $receivers")

        val data = receivers.mapNotNull { receiverName ->
            val receiverClass = Class.forName(receiverName)
            if (!GlanceAppWidgetReceiver::class.java.isAssignableFrom(receiverClass)) {
                return@mapNotNull null
            }
            val receiver = receiverClass.getDeclaredConstructor()
                .newInstance() as GlanceAppWidgetReceiver
            val provider = receiver.glanceAppWidget.javaClass

//            manager.getGlanceIds(provider).map { id ->
//                manager.getAppWidgetSizes(id)
//            }
//            ProviderData(
//                provider = provider,
//                receiver = receiver.javaClass,
//                appWidgets = manager.getGlanceIds(provider).map { id ->
//                    AppWidgetDesc(appWidgetId = id, sizes = manager.getAppWidgetSizes(id))
//                })


        }

        Timber.d("${receivers.joinToString { it }} -- ")
        setContent {
            val vm: ConfigurationViewModel = hiltViewModel()
            val project by vm.project.collectAsState(null)

            var projectId by remember(project?.name) { mutableStateOf(project?.name ?: "") }
            val context = LocalContext.current
            val coroutineScope = rememberCoroutineScope()

            fun closeConfiguration() {
                val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                coroutineScope.launch {
                    DailyAlbumWidget().updateAll(context)
                }
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

//                    TextField(
//                        modifier = Modifier
//                            .fillMaxWidth(),
//                        label = { Text("Group name") },
//                        singleLine = true,
//                        value = groupId,
//                        onValueChange = { groupId = it })

                    TextField(
                        modifier = Modifier
                            .fillMaxWidth(),
                        label = { Text("Project name (username)") },
                        singleLine = true,
                        value = projectId,
                        onValueChange = { projectId = it })


                    Button(
                        onClick = {
//                            if (groupId.isNotBlank()) {
//                                vm.setGroupId(groupId)
//                            } else
                            if (projectId.isNotBlank()) {
                                vm.setProjectId(projectId)
                            }

                            scope.launch {
                                keyboardController?.hide()
                            }

                        },
                        enabled = //groupId.isNotBlank() ||
                        projectId.isNotBlank()
                    ) {
                        Text("Click to set project")
//                        Text("Click to set ${if (groupId.isNotBlank()) "Group" else if (projectId.isNotBlank()) "Project" else "Nothing"}")
                    }

                    if (project != null) {
                        val currentAlbum = project?.currentAlbum!!
                        val currentCoverImage = currentAlbum.images.maxBy { it.height + it.width }?.url
                        Column(modifier = Modifier.padding(16.dp)) {
                            AsyncImage(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                model = currentCoverImage, contentDescription = "Current Album"
                            )
                            Text("${currentAlbum.artist} - ${currentAlbum.name}")
                        }

                        Button(onClick = ::closeConfiguration) {
                            Text("Apply changes")
                        }
                    }


//                    if (group != null) {
//                        Button(onClick = ::closeConfiguration) {
//                            Text("Apply changes")
//                        }
//                    }

                }
            }
        }
    }
}
