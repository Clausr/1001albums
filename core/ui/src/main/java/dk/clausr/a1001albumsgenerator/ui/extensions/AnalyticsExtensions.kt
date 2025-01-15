package dk.clausr.a1001albumsgenerator.ui.extensions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import dk.clausr.a1001albumsgenerator.analytics.AnalyticsEvent
import dk.clausr.a1001albumsgenerator.analytics.AnalyticsHelper
import dk.clausr.a1001albumsgenerator.analytics.LocalAnalyticsHelper

fun AnalyticsHelper.logScreenView(
    screenName: String,
    extras: List<AnalyticsEvent.Param> = emptyList(),
) {
    logEvent(
        AnalyticsEvent(
            type = AnalyticsEvent.Types.SCREEN_VIEW,
            extras = listOf(
                AnalyticsEvent.Param(AnalyticsEvent.ParamKeys.SCREEN_NAME, screenName),
            ) + extras,
        ),
    )
}

fun AnalyticsHelper.logRatingGiven(
    gaveRating: Boolean,
) {
    logEvent(
        AnalyticsEvent(
            type = AnalyticsEvent.Types.RATE_ALBUM,
            extras = listOf(
                AnalyticsEvent.Param(AnalyticsEvent.ParamKeys.RATING, gaveRating.toString())
            )
        )
    )
}

/**
 * A side-effect which records a screen view event.
 */
@Composable
fun TrackScreenViewEvent(
    screenName: String,
    extras: List<AnalyticsEvent.Param> = emptyList(),
    analyticsHelper: AnalyticsHelper = LocalAnalyticsHelper.current,
) = DisposableEffect(Unit) {
    analyticsHelper.logScreenView(
        screenName = screenName,
        extras = extras
    )
    onDispose {}
}
