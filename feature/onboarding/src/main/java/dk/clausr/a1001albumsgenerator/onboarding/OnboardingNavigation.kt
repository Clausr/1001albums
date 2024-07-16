package dk.clausr.a1001albumsgenerator.onboarding

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import dk.clausr.a1001albumsgenerator.onboarding.screens.AppScreen
import dk.clausr.a1001albumsgenerator.onboarding.screens.ProjectScreen
import dk.clausr.a1001albumsgenerator.onboarding.screens.WidgetScreen

object OnboardingDirections {
    private const val prefix = "onboarding"

    object ROUTES {
        const val PROJECT = "$prefix/project"
        const val WIDGET = "$prefix/widget"
        const val APP = "$prefix/app"
    }

    fun project() = ROUTES.PROJECT
    fun widget() = ROUTES.WIDGET
    fun app() = ROUTES.APP
}

fun NavGraphBuilder.onboardingNavigationGraph(
    navHostController: NavHostController,
    navigateToMainApp: () -> Unit,
) {
    composable(route = OnboardingDirections.app()) {
        AppScreen(
            clickNext = {
                navHostController.navigate(OnboardingDirections.widget())
            }
        )
    }

    composable(route = OnboardingDirections.widget()) {
        WidgetScreen(navigateUp = { navHostController.navigateUp() },
            navigateNext = { navHostController.navigate(OnboardingDirections.project()) })
    }

    composable(route = OnboardingDirections.project()) {
        ProjectScreen(
            goBack = {
                navHostController.navigateUp()
            },
            navigateToMainApp = navigateToMainApp
        )
    }
}