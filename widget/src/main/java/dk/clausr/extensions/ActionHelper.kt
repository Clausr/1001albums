package dk.clausr.extensions

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import androidx.glance.action.Action
import androidx.glance.appwidget.action.actionStartActivity

fun openUrlAction(url: String): Action {
    val urlIntent = Intent(Intent.ACTION_VIEW).apply {
        data = url.toUri()
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    return actionStartActivity(urlIntent)
}

/** Boolean extra on the MainActivity intent: open the notification sheet once the app is shown. */
const val EXTRA_OPEN_NOTIFICATIONS = "dk.clausr.extra.OPEN_NOTIFICATIONS"

fun openNotificationsAction(context: Context): Action {
    val intent = Intent(context, Class.forName("dk.clausr.a1001albumsgenerator.MainActivity")).apply {
        setPackage(context.packageName)
        flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
        putExtra(EXTRA_OPEN_NOTIFICATIONS, true)
    }

    return actionStartActivity(intent)
}
