package dk.clausr.a1001albumsgenerator.navigation

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import dk.clausr.configuration.WidgetConfigurationRoute
import dk.clausr.feature.overview.OverviewRoute

object MainDirections {
    private const val PREFIX = "main"

    object ROUTES {
        const val HOME = "$PREFIX/home"
        const val WIDGET_CONFIGURATION = "$PREFIX/configuration"
    }

    fun home() = ROUTES.HOME
    fun widgetConfiguration() = ROUTES.WIDGET_CONFIGURATION
}

fun NavGraphBuilder.mainNavigationGraph(navHostController: NavHostController) {
    composable(route = MainDirections.ROUTES.HOME) {
        OverviewRoute(
            modifier = Modifier,
            onConfigureWidget = {
                navHostController.navigate(MainDirections.ROUTES.WIDGET_CONFIGURATION)
            },
        )
    }

    composable(route = MainDirections.ROUTES.WIDGET_CONFIGURATION) {
        WidgetConfigurationRoute(
            onProjectIdSet = { },
            onApplyChanges = {
                navHostController.navigate(MainDirections.home()) {
                    this.popUpTo(MainDirections.ROUTES.WIDGET_CONFIGURATION) {
                        inclusive = true
                    }
                }
                navHostController.navigateUp()
            },
        )
    }
}
