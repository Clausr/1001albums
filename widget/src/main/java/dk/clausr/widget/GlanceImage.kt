package dk.clausr.widget

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmapOrNull
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.layout.ContentScale
import androidx.glance.layout.fillMaxSize
import coil3.imageLoader
import coil3.request.ErrorResult
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowHardware
import coil3.toBitmap
import timber.log.Timber

@Composable
fun GlanceImage(
    src: String,
    modifier: GlanceModifier = GlanceModifier,
    contentDescription: String? = null,
) {
    val context = LocalContext.current
    val isPreview = LocalInspectionMode.current
    var image by remember(src) {
        val placeholder = if (isPreview) {
            ResourcesCompat.getDrawable(
                context.resources,
                R.drawable.heart_broken,
                null,
            )?.toBitmapOrNull()
        } else {
            null
        }
        mutableStateOf(placeholder)
    }

    // Skip loading the image if we're in a preview, otherwise the preview rendering will
    // block forever waiting for the image to load.
    if (!isPreview) {
        LaunchedEffect(src) {
            val request = ImageRequest.Builder(context)
                .data(src)
                .allowHardware(false)
                .build()
            when (val result = context.imageLoader.execute(request)) {
                is SuccessResult -> {
                    image = result.image.toBitmap()
                }

                is ErrorResult -> {
                    Timber.e(result.throwable, "Error loading image $src")
                }
            }
        }
    }

    image?.let {
        Image(
            provider = ImageProvider(it),
            contentDescription = contentDescription,
            modifier = modifier.fillMaxSize(),
            contentScale = ContentScale.Fit,
        )
    }
}
