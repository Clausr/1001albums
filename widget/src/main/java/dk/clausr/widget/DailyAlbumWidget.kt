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
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmapOrNull
import androidx.core.net.toUri
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.CircularProgressIndicator
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import coil.imageLoader
import coil.request.CachePolicy
import coil.request.ErrorResult
import coil.request.ImageRequest
import coil.request.SuccessResult
import dagger.hilt.EntryPoints
import dagger.hilt.android.AndroidEntryPoint
import dk.clausr.core.data.repository.OagRepository
import dk.clausr.worker.UpdateWidgetWorker
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

object DailyAlbumWidget : GlanceAppWidget() {
    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) = provideContent {
        GlanceTheme {
            MyContent()
        }
    }

    @Composable
    fun MyContent() {
        val context = LocalContext.current

        val viewModel = remember {
            EntryPoints.get(context, OagEntryPoint::class.java).vm()
        }

        val widgetState by viewModel.widgetState.collectAsState(initial = WidgetState.Loading)

        when (val state = widgetState) {
            WidgetState.Error -> ErrorState {
                viewModel.refresh()
            }

            WidgetState.Loading -> LoadingState {
                viewModel.refresh()
            }

            is WidgetState.RateYesterday -> RateYesterdaysAlbum(
                coverUrl = state.coverUrl,
                projectId = state.projectId,
            )

            is WidgetState.TodaysAlbum -> CurrentAlbum(
                coverUrl = state.coverUrl, projectId = state.projectId
            )
        }
    }

    @Composable
    private fun ErrorState(retry: () -> Unit) {
        Box(
            GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.errorContainer)
                .clickable(retry),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                modifier = GlanceModifier.fillMaxSize().padding(20.dp),
                provider = ImageProvider(R.drawable.heart_broken),
                contentDescription = "Error",
                colorFilter = ColorFilter.tint(GlanceTheme.colors.onErrorContainer)
            )
        }
    }

    @Composable
    fun CurrentAlbum(coverUrl: String, projectId: String) {
        val context = LocalContext.current
        Box(
            modifier = GlanceModifier.fillMaxSize().clickable {
                context.openWebsite(projectId)
            },
            contentAlignment = Alignment.BottomCenter
        ) {
            CoverImage(
                modifier = GlanceModifier.fillMaxSize(),
                coverUrl = coverUrl
            )
        }
    }

    private fun Context.openWebsite(projectId: String) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = "https://1001albumsgenerator.com/$projectId".toUri()
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
    }

    @Composable
    fun RateYesterdaysAlbum(
        coverUrl: String,
        projectId: String
    ) {
        val context = LocalContext.current
        CoverImage(
            modifier = GlanceModifier.fillMaxSize(),
            coverUrl = coverUrl,
            tint = ColorProvider(GlanceTheme.colors.background.getColor(context).copy(alpha = 0.75f))
        )

        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .clickable {
                    context.openWebsite(projectId)
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Click to rate",
                modifier = GlanceModifier
                    .fillMaxWidth(),
                style = TextStyle(
                    color = GlanceTheme.colors.onBackground,
                    fontSize = TextUnit(16f, TextUnitType.Sp),
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
            )
        }
    }

    @Composable
    fun CoverImage(
        modifier: GlanceModifier = GlanceModifier,
        coverUrl: String,
        tint: ColorProvider? = null
    ) {
        var coverBitmap by remember { mutableStateOf<Bitmap?>(null) }
        val context = LocalContext.current
        LaunchedEffect(coverUrl) {
            coverBitmap = context.getImage(coverUrl)
        }

        coverBitmap?.let {
            Image(
                provider = ImageProvider(it),
                contentDescription = "Image from Picsum Photos",
                contentScale = ContentScale.Fit,
                colorFilter = tint?.let { ColorFilter.tint(it) },
                modifier = modifier
            )
        }
    }

    @Composable
    fun LoadingState(onClick: () -> Unit) {
        Column(modifier = GlanceModifier.background(GlanceTheme.colors.background).clickable {
            onClick()
        }) {
            CircularProgressIndicator()
            Text("Loading cover image...")
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
    override val glanceAppWidget: GlanceAppWidget = DailyAlbumWidget

    @Inject
    lateinit var oagRepository: OagRepository

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        //Start daily job
        val projectId =
            runBlocking { oagRepository.projectId.first() } ?: error("No projectId when starting")
        UpdateWidgetWorker.start(context, projectId = projectId)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        // Stop daily job
        UpdateWidgetWorker.cancel(context)
    }
}
