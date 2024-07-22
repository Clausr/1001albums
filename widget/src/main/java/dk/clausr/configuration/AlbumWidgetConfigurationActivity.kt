package dk.clausr.configuration

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint
import dk.clausr.a1001albumsgenerator.settings.SettingsRoute

@AndroidEntryPoint
class AlbumWidgetConfigurationActivity : ComponentActivity() {

    private val viewModel: ConfigurationViewModel by viewModels()

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
            SettingsRoute(
                showBack = false,
                onNavigateUp = {},
                onClickApply = {
                    // Start updates and stuff
                    viewModel.updateWidgets()

                    val resultValue = Intent().putExtra(
                        AppWidgetManager.EXTRA_APPWIDGET_ID,
                        appWidgetId,
                    )
                    setResult(RESULT_OK, resultValue)
                    finish()
                },
            )
        }
    }
}
