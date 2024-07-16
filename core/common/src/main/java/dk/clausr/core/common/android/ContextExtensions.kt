package dk.clausr.core.common.android

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri

fun Context.openLink(url: String) {
    val urlIntent = Intent(Intent.ACTION_VIEW).apply {
        data = url.toUri()
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    startActivity(urlIntent)
}
