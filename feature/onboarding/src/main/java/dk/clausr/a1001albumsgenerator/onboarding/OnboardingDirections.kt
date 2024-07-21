package dk.clausr.a1001albumsgenerator.onboarding

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import dk.clausr.a1001albumsgenerator.onboarding.screens.ProjectRoute
import dk.clausr.a1001albumsgenerator.onboarding.screens.WidgetScreen

object OnboardingDirections {
    object Routes {
        internal const val ONBOARDING_ROOT = "onboarding"
        internal const val PROJECT = "$ONBOARDING_ROOT/project"
        internal const val WIDGET = "$ONBOARDING_ROOT/widget"
        internal const val INTRO = "$ONBOARDING_ROOT/intro"
    }

    fun onboarding() = Routes.ONBOARDING_ROOT
    fun project() = Routes.PROJECT
    fun widget() = Routes.WIDGET
    fun intro() = Routes.INTRO
}

fun NavGraphBuilder.onboardingNavigationGraph(navHostController: NavHostController) {

    navigation(
        route = OnboardingDirections.Routes.ONBOARDING_ROOT,
        startDestination = OnboardingDirections.Routes.INTRO,
    ) {
        composable(route = OnboardingDirections.intro()) {
            OnboardingRoute()
        }

        composable(route = OnboardingDirections.widget()) {
            WidgetScreen(
                navigateUp = { navHostController.navigateUp() },
                navigateNext = { navHostController.navigate(OnboardingDirections.project()) },
            )
        }

        composable(route = OnboardingDirections.project()) {
            ProjectRoute(
                navigateUp = {
                    navHostController.navigateUp()
                },
            )
        }
    }
}