package dk.clausr.a1001albumsgenerator.ui.extensions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import dk.clausr.a1001albumsgenerator.analytics.AnalyticsEvent
import dk.clausr.a1001albumsgenerator.analytics.AnalyticsHelper
import dk.clausr.a1001albumsgenerator.analytics.LocalAnalyticsHelper
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

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

fun AnalyticsHelper.logRatingGiven(gaveRating: Boolean) {
    logEvent(
        AnalyticsEvent(
            type = AnalyticsEvent.Types.RATE_ALBUM,
            extras = listOf(
                AnalyticsEvent.Param(AnalyticsEvent.ParamKeys.RATING, gaveRating.toString()),
            ),
        ),
    )
}

fun AnalyticsHelper.logClickEvent(eventName: String) {
    logEvent(
        AnalyticsEvent(
            type = AnalyticsEvent.Types.CLICK_ITEM,
            extras = listOf(
                AnalyticsEvent.Param(AnalyticsEvent.ParamKeys.EVENT_NAME, eventName),
            ),
        ),
    )
}

fun AnalyticsHelper.logListItemSelected(
    listName: String,
    itemName: String,
) {
    logEvent(
        AnalyticsEvent(
            type = AnalyticsEvent.Types.SELECT_ITEM,
            extras = listOf(
                AnalyticsEvent.Param(AnalyticsEvent.ParamKeys.ITEM_LIST_NAME, listName),
                AnalyticsEvent.Param(AnalyticsEvent.ParamKeys.ITEM_NAME, itemName),
            ),
        ),
    )
}

fun AnalyticsHelper.logAlbumSwiped(
    listName: String,
    direction: String,
) {
    logEvent(
        AnalyticsEvent(
            type = AnalyticsEvent.Types.SWIPE_ALBUM,
            extras = listOf(
                AnalyticsEvent.Param(AnalyticsEvent.ParamKeys.ITEM_LIST_NAME, listName),
                AnalyticsEvent.Param(AnalyticsEvent.ParamKeys.DIRECTION, direction),
            ),
        ),
    )
}

/** Logs which type of filter was used in search; never the selected values or the query. */
fun AnalyticsHelper.logSearchFilterUsed(filterType: String) {
    logEvent(
        AnalyticsEvent(
            type = AnalyticsEvent.Types.SEARCH_FILTER,
            extras = listOf(
                AnalyticsEvent.Param(AnalyticsEvent.ParamKeys.FILTER_TYPE, filterType),
            ),
        ),
    )
}

/**
 * A side-effect which records a screen view event.
 */
@Composable
fun TrackScreenViewEvent(
    screenName: String,
    extras: ImmutableList<AnalyticsEvent.Param> = persistentListOf(),
    analyticsHelper: AnalyticsHelper = LocalAnalyticsHelper.current,
) = DisposableEffect(Unit) {
    analyticsHelper.logScreenView(
        screenName = screenName,
        extras = extras,
    )
    onDispose {}
}

@Composable
fun TrackClickedEvent(
    event: String,
    analyticsHelper: AnalyticsHelper = LocalAnalyticsHelper.current,
) = DisposableEffect(Unit) {
    analyticsHelper.logClickEvent(event)
    onDispose {}
}
