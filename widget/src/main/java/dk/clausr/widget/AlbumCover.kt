package dk.clausr.widget

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.graphics.drawable.toBitmapOrNull
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.layout.ContentScale
import coil.imageLoader
import coil.request.CachePolicy
import coil.request.ErrorResult
import coil.request.ImageRequest
import coil.request.SuccessResult

@Composable
fun AlbumCover(
    modifier: GlanceModifier = GlanceModifier,
    coverUrl: String,
) {
    var coverBitmap by remember { mutableStateOf<Bitmap?>(null) }
    val context = LocalContext.current

    LaunchedEffect(coverUrl) {
        coverBitmap = context.getImage(coverUrl)
    }

    coverBitmap?.let {
        Image(
            provider = ImageProvider(it),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = modifier
        )
    }
}

private suspend fun Context.getImage(url: String, force: Boolean = false): Bitmap? {
    val request = ImageRequest
        .Builder(this)
        .data(url)
        .apply {
            if (force) {
                memoryCachePolicy(CachePolicy.DISABLED)
                diskCachePolicy(CachePolicy.DISABLED)
            }
        }
        .build()

    // Request the image to be loaded and throw error if it failed
    return when (val result = imageLoader.execute(request)) {
        is ErrorResult -> throw result.throwable
        is SuccessResult -> result.drawable.toBitmapOrNull()
    }
}
