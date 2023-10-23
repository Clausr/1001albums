package dk.clausr.a1001albumsgenerator.ui.glance

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import dk.clausr.a1001albumsgenerator.databinding.AlbumConfiguratorBinding
import dk.clausr.a1001albumsgenerator.ui.theme._1001AlbumsGeneratorTheme

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
            _1001AlbumsGeneratorTheme {

                var group by remember { mutableStateOf("") }
                LaunchedEffect(key1 = group, block = {
                    if (group.equals("test", ignoreCase = true)) {
                        val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)

                        setResult(Activity.RESULT_OK, resultValue)
                        finish()
                    }
                })
                Column(
                    Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    Text("Hej hej")
                    TextField(
                        modifier = Modifier
                            .systemBarsPadding()
                            .fillMaxWidth(),
                        value = group,
                        onValueChange = { group = it })
                }
            }
        }

    }

}