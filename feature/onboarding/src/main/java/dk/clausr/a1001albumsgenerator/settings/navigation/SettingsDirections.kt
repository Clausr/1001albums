package dk.clausr.a1001albumsgenerator.settings.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.navOptions
import androidx.navigation.navigation
import dk.clausr.a1001albumsgenerator.settings.LogScreen
import dk.clausr.a1001albumsgenerator.settings.SettingsScreen
import kotlinx.serialization.Serializable

@Serializable
data object SettingsBaseRoute

@Serializable
data object SettingsRoute

@Serializable
data object DebugLogsRoute

fun NavController.navigateToSettings(navOptions: NavOptions = navOptions { }) =
    navigate(route = SettingsRoute, navOptions = navOptions)

fun NavController.navigateToDebugLogs(navOptions: NavOptions = navOptions { }) =
    navigate(route = DebugLogsRoute, navOptions = navOptions)

fun NavGraphBuilder.settingsGraph(navHostController: NavHostController) {
    navigation<SettingsBaseRoute>(startDestination = SettingsRoute) {
        composable<SettingsRoute> {
            SettingsScreen(
                onNavigateUp = navHostController::navigateUp,
                onClickApply = navHostController::navigateUp,
                onShowLogs = navHostController::navigateToDebugLogs,
            )
        }

        composable<DebugLogsRoute> {
            LogScreen()
        }
    }
}