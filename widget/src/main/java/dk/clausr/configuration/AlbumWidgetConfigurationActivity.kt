package dk.clausr.configuration

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.rememberCoroutineScope
import androidx.glance.appwidget.GlanceAppWidgetManager
import dagger.hilt.android.AndroidEntryPoint
import dk.clausr.a1001albumsgenerator.analytics.AnalyticsHelper
import dk.clausr.a1001albumsgenerator.analytics.LocalAnalyticsHelper
import dk.clausr.a1001albumsgenerator.settings.SettingsRoute
import dk.clausr.a1001albumsgenerator.ui.extensions.TrackScreenViewEvent
import dk.clausr.a1001albumsgenerator.ui.extensions.logClickEvent
import dk.clausr.widget.AlbumCoverWidget
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AlbumWidgetConfigurationActivity : ComponentActivity() {

    private val viewModel: ConfigurationViewModel by viewModels()

    @Inject
    lateinit var analyticsHelper: AnalyticsHelper

    private val appWidgetId: Int by lazy {
        intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID,
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID
    }

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
            CompositionLocalProvider(
                LocalAnalyticsHelper provides analyticsHelper
            ) {
                TrackScreenViewEvent("AlbumWidgetConfigurationScreen")
                val coroutineScope = rememberCoroutineScope()

                SettingsRoute(
                    showBack = false,
                    onNavigateUp = {},
                    onShowLogs = {},
                    onClickApply = {

                        analyticsHelper.logClickEvent("Done")
                        // Start updates and stuff
                        viewModel.updateWidgets()

                        val resultValue = Intent().putExtra(
                            AppWidgetManager.EXTRA_APPWIDGET_ID,
                            appWidgetId,
                        )
                        setResult(RESULT_OK, resultValue)
                        coroutineScope.launch(Dispatchers.IO) {
                            val glanceId = GlanceAppWidgetManager(baseContext).getGlanceIdBy(appWidgetId)
                            AlbumCoverWidget().update(
                                context = this@AlbumWidgetConfigurationActivity,
                                id = glanceId,
                            )
                        }
                        finish()
                    },
                )
            }
        }
    }
}
