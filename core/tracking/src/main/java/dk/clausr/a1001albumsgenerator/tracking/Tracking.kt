package dk.clausr.a1001albumsgenerator.tracking

import android.content.Context
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.logEvent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Tracking @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val firebaseAnalytics by lazy {
        FirebaseAnalytics.getInstance(context)
    }

    fun selectItem(listName: String) {
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
            param(FirebaseAnalytics.Param.ITEM_LIST_NAME, listName)
        }
    }

    fun screenViewed(screenName: String) {
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
        }
    }
}