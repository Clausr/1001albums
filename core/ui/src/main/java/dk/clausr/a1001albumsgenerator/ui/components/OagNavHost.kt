package dk.clausr.a1001albumsgenerator.ui.components

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import dk.clausr.a1001albumsgenerator.ui.navigation.NavHostTransitions

val LocalNavAnimatedVisibilityScope = compositionLocalOf<AnimatedVisibilityScope> {
    error("No Navigation VisibilityScope found!")
}
val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope> {
    error("No shared transition scope found. Remember to set it at NavHost")
}

@Composable
fun OagNavHost(
    navController: NavHostController,
    startDestination: Any,
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.Center,
    isCrossfadeDestination: (NavDestination) -> Boolean = { false },
    builder: NavGraphBuilder.() -> Unit,
) {
    val transitions = remember(isCrossfadeDestination) { NavHostTransitions(isCrossfadeDestination) }
    SharedTransitionLayout {
        CompositionLocalProvider(
            LocalSharedTransitionScope provides this,
        ) {
            NavHost(
                navController = navController,
                startDestination = startDestination,
                modifier = modifier,
                contentAlignment = contentAlignment,
                enterTransition = transitions.enter,
                exitTransition = transitions.exit,
                popEnterTransition = transitions.popEnter,
                popExitTransition = transitions.popExit,
                predictivePopEnterTransition = transitions.predictivePopEnter,
                predictivePopExitTransition = transitions.predictivePopExit,
                builder = builder,
            )
        }
    }
}
