package dk.clausr

import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetProviderInfo
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.glance.ExperimentalGlanceApi
import androidx.glance.appwidget.runComposition
import dk.clausr.widget.AlbumCoverWidget
import kotlinx.coroutines.Dispatchers

class WidgetViewerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val widgets = listOf(
                AlbumCoverWidget()
            )

            var selectedWidget by remember {
                mutableStateOf(widgets.first())
            }
            Column(Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.3F)
                ) {
                    items(widgets) { widget ->
                        Text(
                            text = widget::class.simpleName.orEmpty(),
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .clickable { selectedWidget = widget }
                        )
                    }
                }
                Box(
                    Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.7F)
                        .padding(8.dp)
                ) {
                    WidgetView(selectedWidget, DpSize(500.dp, 500.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalGlanceApi::class)
@Composable
fun WidgetView(widget: AlbumCoverWidget, size: DpSize = DpSize(500.dp, 500.dp)) {
    val context = LocalContext.current
    val remoteViews by
    remember(widget, size) { widget.runComposition(context, sizes = listOf(size)) }
        .collectAsState(null, Dispatchers.Default)
    AndroidView(
        factory = {
            // Using an AWHV is necessary for ListView support, and to properly select a RemoteViews
            // from a multi-size RemoteViews. If the RemoteViews has only one size and does not
            // contain lazy list items, a FrameLayout works fine.
            AppWidgetHostView(context).apply { setFakeAppWidget() }
        },
        modifier = Modifier.fillMaxSize(),
        update = { view -> view.updateAppWidget(remoteViews) },
    )
}


/**
 * The hostView requires an AppWidgetProviderInfo to work in certain OS versions. This method uses
 * reflection to set a fake provider info.
 */
private fun AppWidgetHostView.setFakeAppWidget() {
    val context = context
    val info =
        AppWidgetProviderInfo().apply {
            initialLayout = androidx.glance.appwidget.R.layout.glance_default_loading_layout
        }
    try {
        val activityInfo =
            ActivityInfo().apply {
                applicationInfo = context.applicationInfo
                packageName = context.packageName
                labelRes = applicationInfo.labelRes
            }

        info::class.java.getDeclaredField("providerInfo").run {
            isAccessible = true
            set(info, activityInfo)
        }

        this::class.java.getDeclaredField("mInfo").apply {
            isAccessible = true
            set(this@setFakeAppWidget, info)
        }
    } catch (e: Exception) {
        throw IllegalStateException("Couldn't set fake provider", e)
    }
}
