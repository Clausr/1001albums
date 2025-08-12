package dk.clausr.a1001albumsgenerator

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import dk.clausr.a1001albumsgenerator.navigation.MainAppNavHost

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun OagApp(
    uiState: MainViewState,
    navHostController: NavHostController = rememberNavController(),
) {
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { padding ->
        MainAppNavHost(
            uiState = uiState,
            navHostController = navHostController,
            modifier = Modifier
                .padding(padding)
                .consumeWindowInsets(padding),
        )
    }
}
