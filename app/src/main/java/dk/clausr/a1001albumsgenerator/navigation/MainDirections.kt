@file:OptIn(ExperimentalSharedTransitionApi::class)

package dk.clausr.a1001albumsgenerator.navigation

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import dk.clausr.a1001albumsgenerator.settings.SettingsRoute
import dk.clausr.feature.overview.overviewGraph

object MainDirections {
    private const val PREFIX = "main"

    object ROUTES {
        const val HOME = "$PREFIX/home"
        const val WIDGET_CONFIGURATION = "$PREFIX/configuration"
    }

    fun home() = ROUTES.HOME
    fun widgetConfiguration() = ROUTES.WIDGET_CONFIGURATION
}

fun NavGraphBuilder.mainNavigationGraph(
    navHostController: NavHostController,
    sharedTransitionScope: SharedTransitionScope,
) {
    overviewGraph(
        navHostController = navHostController,
        sharedTransitionScope = sharedTransitionScope,
        navigateToSettings = {
            navHostController.navigate(MainDirections.widgetConfiguration())
        }
    )

    composable(route = MainDirections.widgetConfiguration()) {
        SettingsRoute(
            onNavigateUp = navHostController::navigateUp,
            onClickApply = navHostController::navigateUp,
        )
    }
}
