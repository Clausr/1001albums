package dk.clausr.extensions

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri

fun Context.openProject(projectId: String, rating: Int? = null) {
    val prefilledQuery = rating?.let { "?prefilledRating=$rating" }.orEmpty()
    val intent = Intent(Intent.ACTION_VIEW).apply {
        data = ("https://1001albumsgenerator.com/$projectId$prefilledQuery").toUri()
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    startActivity(intent)
}
