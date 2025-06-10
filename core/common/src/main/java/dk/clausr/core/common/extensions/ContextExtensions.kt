package dk.clausr.core.common.extensions

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.net.toUri
import dk.clausr.core.common.ExternalLinks

fun Context.openProject(
    projectId: String,
    rating: Int? = null,
) {
    val baseUri = ExternalLinks.Generator.BASE_URL
    val uri = baseUri.toUri().buildUpon()
        .appendPath(projectId)
        .apply {
            rating?.let {
                appendQueryParameter("prefilledRating", it.toString())
            }
        }
        .build()

    openLink(uri.toString())
}

fun Context.openLink(url: String) = openLink(url.toUri())

fun Context.openLink(uri: Uri) {
    val urlIntent = Intent(Intent.ACTION_VIEW).apply {
        data = uri
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    startActivity(urlIntent)
}
