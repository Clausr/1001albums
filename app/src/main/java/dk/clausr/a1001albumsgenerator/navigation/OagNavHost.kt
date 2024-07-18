package dk.clausr.a1001albumsgenerator.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
    startDestination: String = remember(uiState) {
        if (uiState is MainViewState.NoProject) {
            OnboardingDirections.onboarding()
        } else {
            MainDirections.home()
        }
    },
) {
    dk.clausr.a1001albumsgenerator.ui.components.OagNavHost(
        navController = navHostController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        onboardingNavigationGraph(navHostController)
        mainNavigationGraph(navHostController)
    }
}
