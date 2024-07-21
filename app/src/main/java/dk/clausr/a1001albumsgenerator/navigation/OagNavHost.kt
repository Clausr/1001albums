package dk.clausr.a1001albumsgenerator.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import dk.clausr.a1001albumsgenerator.MainViewState
import dk.clausr.a1001albumsgenerator.onboarding.OnboardingRoute

@Composable
fun OagNavHost(
    uiState: MainViewState,
    navHostController: NavHostController,
    modifier: Modifier = Modifier,
) {
    if (uiState !is MainViewState.Loading) {
        when (uiState) {
            MainViewState.Loading -> {}
            is MainViewState.Success -> {
                if (uiState.hasOnboarded) {
                    dk.clausr.a1001albumsgenerator.ui.components.OagNavHost(
                        navController = navHostController,
                        startDestination = MainDirections.home(),
                        modifier = modifier,
                    ) {
                        mainNavigationGraph(navHostController)
                    }
                } else {
                    OnboardingRoute()
                }
            }
        }
    }
}
