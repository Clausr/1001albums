package dk.clausr.a1001albumsgenerator.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import dk.clausr.a1001albumsgenerator.settings.LogScreen
import dk.clausr.a1001albumsgenerator.settings.SettingsRoute
import dk.clausr.feature.overview.navigation.overviewGraph

object MainDirections {
    private const val PREFIX = "main"

    object Routes {
        const val HOME = "$PREFIX/home"
        const val WIDGET_CONFIGURATION = "$PREFIX/configuration"
        const val LOGS = "$PREFIX/logs"
    }

    fun home() = Routes.HOME
    fun widgetConfiguration() = Routes.WIDGET_CONFIGURATION
    fun logScreen() = Routes.LOGS
}

fun NavGraphBuilder.mainNavigationGraph(navHostController: NavHostController) {
    overviewGraph(
        navHostController = navHostController,
        navigateToSettings = {
            navHostController.navigate(MainDirections.widgetConfiguration())
        },
    )

    composable(route = MainDirections.widgetConfiguration()) {
        SettingsRoute(
            onNavigateUp = navHostController::navigateUp,
            onClickApply = navHostController::navigateUp,
            onShowLogs = { navHostController.navigate(MainDirections.Routes.LOGS) }
        )
    }

    composable(route = MainDirections.Routes.LOGS) {
        LogScreen()
    }
}
