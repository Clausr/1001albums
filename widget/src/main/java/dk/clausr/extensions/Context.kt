package dk.clausr.extensions

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import androidx.glance.GlanceModifier
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionStartActivity

fun Context.openWithPrefilledRating(projectId: String, rating: Int) {
    val intent = Intent(Intent.ACTION_VIEW).apply {
        data = "https://1001albumsgenerator.com/$projectId?prefilledRating=$rating".toUri()
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    startActivity(intent)
}

fun GlanceModifier.clickableOpenUrl(url: String?): GlanceModifier {
    val urlIntent = Intent(Intent.ACTION_VIEW).apply {
        data = url?.toUri()
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    return GlanceModifier.clickable(actionStartActivity(urlIntent)).then(this)
}


fun Context.openSomeActivity(url: String) {
    val i = Intent(Intent.ACTION_VIEW).apply {
        data = url.toUri()
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    startActivity(i)
}