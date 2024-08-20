package dk.clausr.a1001albumsgenerator.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import dk.clausr.a1001albumsgenerator.MainViewState
import dk.clausr.a1001albumsgenerator.onboarding.OnboardingRoute
import dk.clausr.a1001albumsgenerator.ui.components.OagNavHost
import dk.clausr.feature.overview.navigation.OverviewDirections

@Composable
fun MainAppNavHost(
    uiState: MainViewState,
    navHostController: NavHostController,
    modifier: Modifier = Modifier,
) {
    if (uiState !is MainViewState.Loading) {
        when (uiState) {
            MainViewState.Loading -> {}
            is MainViewState.Success -> {
                if (uiState.hasOnboarded) {
                    OagNavHost(
                        navController = navHostController,
                        startDestination = OverviewDirections.root(),
                        modifier = modifier,
                    ) {
                        mainNavigationGraph(
                            navHostController = navHostController,
                        )
                    }
                } else {
                    OnboardingRoute()
                }
            }
        }
    }
}
