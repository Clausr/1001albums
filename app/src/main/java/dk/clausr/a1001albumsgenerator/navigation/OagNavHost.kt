package dk.clausr.a1001albumsgenerator.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import dk.clausr.a1001albumsgenerator.MainViewState
import dk.clausr.a1001albumsgenerator.onboarding.OnboardingDirections
import dk.clausr.a1001albumsgenerator.onboarding.onboardingNavigationGraph

@Composable
fun OagNavHost(
    uiState: MainViewState,
    navHostController: NavHostController,
    modifier: Modifier = Modifier,
) {
    if (uiState !is MainViewState.Loading) {
        val destination = when (uiState) {
            is MainViewState.HasProject -> {
                if (uiState.project != null) {
                    MainDirections.home()
                } else {
                    OnboardingDirections.onboarding()
                }
            }

            MainViewState.Loading -> OnboardingDirections.onboarding()
        }

        dk.clausr.a1001albumsgenerator.ui.components.OagNavHost(
            navController = navHostController,
            startDestination = destination,
            modifier = modifier,
        ) {
            onboardingNavigationGraph(navHostController)
            mainNavigationGraph(navHostController)
        }
    }
}
