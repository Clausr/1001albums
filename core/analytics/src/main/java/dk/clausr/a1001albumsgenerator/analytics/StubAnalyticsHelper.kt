package dk.clausr.a1001albumsgenerator.analytics

import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * An implementation of AnalyticsHelper just writes the events to logcat. Used in builds where no
 * analytics events should be sent to a backend.
 */
@Singleton
internal class StubAnalyticsHelper @Inject constructor() : AnalyticsHelper {
    override fun logEvent(event: AnalyticsEvent) {
        Timber.d("Received analytics event: $event")
    }
}
