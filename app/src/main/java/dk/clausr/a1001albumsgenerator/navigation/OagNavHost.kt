package dk.clausr.a1001albumsgenerator.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import dk.clausr.a1001albumsgenerator.MainViewState
import timber.log.Timber


@Composable
fun OagNavHost(
    uiState: MainViewState,
    navHostController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = MainDirections.home(),
) {
    val dest: String? by remember(uiState) {
        mutableStateOf(
            when (uiState) {
                is MainViewState.HasProject -> MainDirections.home()
                MainViewState.Loading -> null
                MainViewState.NoProject -> MainDirections.widgetConfiguration()
            }
        )
    }

    LaunchedEffect(dest) {
        Timber.d("Dest changed $dest")
    }

    NavHost(
        navController = navHostController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        mainNavigationGraph(navHostController)
    }
}