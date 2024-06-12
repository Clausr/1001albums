package dk.clausr.extensions

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri

fun Context.open1001Website(projectId: String) {
    val intent = Intent(Intent.ACTION_VIEW).apply {
        data = "https://1001albumsgenerator.com/$projectId".toUri()
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    startActivity(intent)
}

fun Context.openSomeActivity(url: String) {
    val i = Intent(Intent.ACTION_VIEW).apply {
        data = url.toUri()
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    startActivity(i)
}