package dk.clausr.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver

@OptIn(ExperimentalMaterial3Api::class)
class AlbumWidgetConfigurationActivity : ComponentActivity() {

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
        Log.d("AlbumWidgetConfigurationActivity", "On resume..")
//        updateView()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.i("AlbumWidgetConfigurationActivity", "Widget ID: $appWidgetId")

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


        val data = receivers.mapNotNull { receiverName ->
            val receiverClass = Class.forName(receiverName)
            if (!GlanceAppWidgetReceiver::class.java.isAssignableFrom(receiverClass)) {
                return@mapNotNull null
            }
            val receiver = receiverClass.getDeclaredConstructor()
                .newInstance() as GlanceAppWidgetReceiver
//            val provider = receiver.glanceAppWidget.javaClass
////            ProviderData(
////                provider = provider,
////                receiver = receiver.javaClass,
////                appWidgets = manager.getGlanceIds(provider).map { id ->
////                    AppWidgetDesc(appWidgetId = id, sizes = manager.getAppWidgetSizes(id))
////                })

        }

        Log.d("AlbumWidget", "${receivers.joinToString { it }} -- ")
        setContent {
            var username by remember { mutableStateOf("") }

            LaunchedEffect(key1 = username,
                block = {
                    if (username.equals("test", ignoreCase = true)) {
                        val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)

                        setResult(Activity.RESULT_OK, resultValue)
                        finish()
                    }
                })

            Scaffold(
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                topBar = {
                    TopAppBar(title = { Text("1001 albums") })
                }
            ) { padding ->
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp)
                        .background(MaterialTheme.colorScheme.background),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    TextField(
                        modifier = Modifier
                            .fillMaxWidth(),
                        label = { Text("Group name") },
                        singleLine = true,
                        value = username,
                        onValueChange = { username = it })
                }
            }
        }
    }

}
