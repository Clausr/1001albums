package dk.clausr.a1001albumsgenerator.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import dk.clausr.a1001albumsgenerator.MainViewState


@Composable
fun OagNavHost(
    uiState: MainViewState,
    navHostController: NavHostController,
    modifier: Modifier = Modifier,
) {
    val startDestination =
        if (uiState is MainViewState.NoProject) {
            MainDirections.widgetConfiguration()
        } else {
            MainDirections.home()
        }


    NavHost(
        navController = navHostController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        mainNavigationGraph(navHostController)
    }
}