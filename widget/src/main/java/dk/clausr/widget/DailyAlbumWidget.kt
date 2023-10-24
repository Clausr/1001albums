package dk.clausr.widget

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmapOrNull
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
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
import dagger.hilt.EntryPoints
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber


class DailyAlbumWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            MyContent()
        }
    }

    @Composable
    fun MyContent() {
        val context = LocalContext.current
        val viewModel = EntryPoints.get(context, OagEntryPoint::class.java).vm()

        val group by viewModel.getGroup().collectAsState(initial = null)

        Timber.d("My Content ran")
        val latestAlbumUrl = group?.currentAlbum?.images?.maxBy { it.height }?.url
        var currentAlbumImage by remember(latestAlbumUrl) { mutableStateOf<Bitmap?>(null) }

        val scope = rememberCoroutineScope()
        LaunchedEffect(latestAlbumUrl) {
            if (latestAlbumUrl != null) {
                currentAlbumImage = context.getImage(latestAlbumUrl)
            }
        }

        if (currentAlbumImage != null) {
            Image(
                provider = ImageProvider(currentAlbumImage!!),
                contentDescription = "Image from Picsum Photos",
                contentScale = ContentScale.Fit,
                modifier = GlanceModifier.cornerRadius(16.dp).fillMaxSize()
                    .clickable {
                        scope.launch {
//                            randomImage = context.getImage(url, force = true)
                        }
                    }
            )
        } else {
            CircularProgressIndicator(modifier = GlanceModifier.clickable {
                scope.launch {
//                    randomImage = context.getImage(url, force = true)
                }
            })
        }
    }

    private suspend fun Context.getImage(url: String, force: Boolean = false): Bitmap? {
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

@AndroidEntryPoint
class AlbumWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = DailyAlbumWidget()

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
    }
}
