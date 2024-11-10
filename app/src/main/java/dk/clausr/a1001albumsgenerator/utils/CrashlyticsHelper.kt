package dk.clausr.a1001albumsgenerator.utils

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber

object CrashlyticsHelper {
    private val firebaseCrashlytics: FirebaseCrashlytics by lazy { FirebaseCrashlytics.getInstance() }

    class CrashlyticsLoggingTree : Timber.Tree() {
        override fun log(
            priority: Int,
            tag: String?,
            message: String,
            t: Throwable?,
        ) {
            when (priority) {
                Log.VERBOSE, Log.DEBUG -> Unit
                Log.ASSERT -> {
                    when (t) {
                        null -> firebaseCrashlytics.log(message)
                        else -> firebaseCrashlytics.recordException(t)
                    }
                }

                Log.INFO, Log.WARN, Log.ERROR -> {
                    firebaseCrashlytics.log(listOfNotNull(tag, message).joinToString(separator = ": "))
                }
            }
        }
    }
}
