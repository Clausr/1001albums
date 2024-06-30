package dk.clausr.a1001albumsgenerator.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import dk.clausr.a1001albumsgenerator.MainViewState
import dk.clausr.a1001albumsgenerator.navigation.OagNavHost
import dk.clausr.a1001albumsgenerator.ui.theme.OagTheme

@Composable
fun OagApp(
    navHostController: NavHostController = rememberNavController(),
    uiState: MainViewState,
) {
    OagTheme {
        Scaffold(
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
        ) { padding ->
            OagNavHost(
                uiState = uiState,
                navHostController = navHostController,
                modifier = Modifier
                    .padding(padding)
                    .consumeWindowInsets(padding),
            )
        }
    }
}