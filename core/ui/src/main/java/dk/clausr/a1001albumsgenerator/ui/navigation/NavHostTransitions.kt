package dk.clausr.a1001albumsgenerator.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination

private typealias Enter = AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition
private typealias Exit = AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition

private const val DURATION = 350
private const val CROSSFADE_DURATION = 700

/** The scale of the screen further back in the stack: where a new screen zooms in from, and an old one zooms out to. */
private const val BACK_SCALE = 0.9f

/** The scale of the screen further forward in the stack. */
private const val FRONT_SCALE = 1.05f

/** navigation-compose's default before 2.10. */
private val CrossfadeSpec = tween<Float>(CROSSFADE_DURATION)

// Material 3 emphasized easing.
private val Emphasized: Easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)

/**
 * Transitions for a NavHost. Navigating forward zooms in towards the new screen, and back zooms out to the previous one.
 *
 * @param isCrossfadeDestination Destinations that keep the plain crossfade whenever they are navigated to or from,
 * e.g. because their shared element transitions are tuned for it.
 */
class NavHostTransitions(private val isCrossfadeDestination: (NavDestination) -> Boolean) {
    private val AnimatedContentTransitionScope<NavBackStackEntry>.crossfade: Boolean
        get() = isCrossfadeDestination(initialState.destination) || isCrossfadeDestination(targetState.destination)

    val enter: Enter = { if (crossfade) fadeIn(CrossfadeSpec) else zoomIn(initialScale = BACK_SCALE, easing = Emphasized) }
    val exit: Exit = { if (crossfade) fadeOut(CrossfadeSpec) else zoomOut(targetScale = FRONT_SCALE, easing = Emphasized) }
    val popEnter: Enter = { if (crossfade) fadeIn(CrossfadeSpec) else zoomIn(initialScale = FRONT_SCALE, easing = Emphasized) }
    val popExit: Exit = { if (crossfade) fadeOut(CrossfadeSpec) else zoomOut(targetScale = BACK_SCALE, easing = Emphasized) }

    // The predictive back gesture scrubs through the pop transition, so it should track the finger linearly.
    val predictivePopEnter: AnimatedContentTransitionScope<NavBackStackEntry>.(Int) -> EnterTransition = {
        if (crossfade) fadeIn(CrossfadeSpec) else zoomIn(initialScale = FRONT_SCALE, easing = LinearEasing)
    }
    val predictivePopExit: AnimatedContentTransitionScope<NavBackStackEntry>.(Int) -> ExitTransition = {
        if (crossfade) fadeOut(CrossfadeSpec) else zoomOut(targetScale = BACK_SCALE, easing = LinearEasing)
    }
}

private fun zoomIn(
    initialScale: Float,
    easing: Easing,
): EnterTransition = scaleIn(tween(DURATION, easing = easing), initialScale = initialScale) +
    fadeIn(tween(DURATION, easing = easing))

private fun zoomOut(
    targetScale: Float,
    easing: Easing,
): ExitTransition = scaleOut(tween(DURATION, easing = easing), targetScale = targetScale) +
    fadeOut(tween(DURATION, easing = easing))
