package dk.clausr.extensions

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