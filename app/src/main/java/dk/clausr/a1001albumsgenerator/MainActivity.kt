package dk.clausr.a1001albumsgenerator

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import dk.clausr.a1001albumsgenerator.analytics.AnalyticsHelper
import dk.clausr.a1001albumsgenerator.analytics.LocalAnalyticsHelper
import dk.clausr.extensions.EXTRA_OPEN_NOTIFICATIONS
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainActivityViewModel by viewModels()

    @Inject
    lateinit var analyticsHelper: AnalyticsHelper

    /** Set when launched from the widget's notification button; cleared once the sheet has been opened. */
    private var openNotificationsRequested by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // On recreation the intent is the original one again, so only read it on a fresh launch
        if (savedInstanceState == null) handleIntent(intent)

        var uiState: MainViewState by mutableStateOf(MainViewState.Loading)

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState
                    .onEach { uiState = it }
                    .collect()
            }
        }

        splashScreen.setKeepOnScreenCondition {
            when (uiState) {
                MainViewState.Loading -> {
                    true
                }

                is MainViewState.Success -> {
                    false
                }
            }
        }

        setContent {
            val navHostController = rememberNavController()

            CompositionLocalProvider(
                LocalAnalyticsHelper provides analyticsHelper,
            ) {
                OagApp(
                    uiState = uiState,
                    navHostController = navHostController,
                    openNotificationsRequested = openNotificationsRequested,
                    onNotificationsOpened = { openNotificationsRequested = false },
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        if (intent.getBooleanExtra(EXTRA_OPEN_NOTIFICATIONS, false)) {
            openNotificationsRequested = true
        }
    }
}
