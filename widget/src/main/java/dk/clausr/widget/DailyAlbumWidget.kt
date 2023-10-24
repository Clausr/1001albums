package dk.clausr.widget

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmapOrNull
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.CircularProgressIndicator
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.ContentScale
import androidx.glance.layout.fillMaxSize
import coil.imageLoader
import coil.request.CachePolicy
import coil.request.ErrorResult
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class DailyAlbumWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            MyContent()
        }
    }

    @Composable
    fun MyContent() {
        val context = LocalContext.current
        val url = getImageUrl(LocalSize.current)
        val scope = rememberCoroutineScope()
        var randomImage by remember(url) { mutableStateOf<Bitmap?>(null) }

        LaunchedEffect(Unit) {
            randomImage = context.getRandomImage(url, force = true)
        }
        // Load a random image
        LaunchedEffect(url) {
            randomImage = context.getRandomImage(url)
        }

        if (randomImage != null) {
            Image(
                provider = ImageProvider(randomImage!!),
                contentDescription = "Image from Picsum Photos",
                contentScale = ContentScale.FillBounds,
                modifier = GlanceModifier.cornerRadius(16.dp).fillMaxSize().clickable {
                    scope.launch {
                        randomImage = context.getRandomImage(url, force = true)
                    }
                }
            )
        } else {
            CircularProgressIndicator(modifier = GlanceModifier)
        }
    }

    private fun getImageUrl(size: DpSize) =
        "https://picsum.photos/${size.width.value.roundToInt()}/${size.height.value.roundToInt()}"

    private suspend fun Context.getRandomImage(url: String, force: Boolean = false): Bitmap? {
        val request = ImageRequest.Builder(this).data(url).apply {
            if (force) {
                memoryCachePolicy(CachePolicy.DISABLED)
                diskCachePolicy(CachePolicy.DISABLED)
            }
        }.build()

        // Request the image to be loaded and throw error if it failed
        return when (val result = imageLoader.execute(request)) {
            is ErrorResult -> throw result.throwable
            is SuccessResult -> result.drawable.toBitmapOrNull()
        }
    }
}

class AlbumWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = DailyAlbumWidget()
}
