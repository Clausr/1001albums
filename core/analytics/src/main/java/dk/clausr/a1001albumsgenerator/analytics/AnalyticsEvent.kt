package dk.clausr.a1001albumsgenerator.analytics

import com.google.firebase.analytics.FirebaseAnalytics

/**
 * Represents an analytics event.
 *
 * @param type - the event type. Wherever possible use one of the standard
 * event `Types`, however, if there is no suitable event type already defined, a custom event can be
 * defined as long as it is configured in your backend analytics system (for example, by creating a
 * Firebase Analytics custom event).
 *
 * @param extras - list of parameters which supply additional context to the event. See `Param`.
 */
data class AnalyticsEvent(
    val type: String,
    val extras: List<Param> = emptyList(),
) {
    // Standard analytics types.
    object Types {
        const val SCREEN_VIEW = FirebaseAnalytics.Event.SCREEN_VIEW // (extras: SCREEN_NAME)
        const val SELECT_ITEM = FirebaseAnalytics.Event.SELECT_ITEM
        const val RATE_ALBUM = "rate_album"
        const val CLICK_ITEM = "click_event"
        const val REFRESH_ACTION = "refresh_content"
    }

    /**
     * A key-value pair used to supply extra context to an analytics event.
     *
     * @param key - the parameter key. Wherever possible use one of the standard `ParamKeys`,
     * however, if no suitable key is available you can define your own as long as it is configured
     * in your backend analytics system (for example, by creating a Firebase Analytics custom
     * parameter).
     *
     * @param value - the parameter value.
     */
    data class Param(val key: String, val value: String)

    // Standard parameter keys.
    object ParamKeys {
        const val SCREEN_NAME = FirebaseAnalytics.Param.SCREEN_NAME
        const val ITEM_LIST_NAME = FirebaseAnalytics.Param.ITEM_LIST_NAME
        const val ITEM_NAME = FirebaseAnalytics.Param.ITEM_LIST_NAME
        const val RATING = "rating"
        const val EVENT_NAME = "event_name"
    }
}
