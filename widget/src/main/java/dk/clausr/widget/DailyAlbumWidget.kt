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
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.CircularProgressIndicator
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.cornerRadius
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
import dk.clausr.WebsiteActivity
import timber.log.Timber


class DailyAlbumWidget : GlanceAppWidget() {
    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

//    fun updateAll(context: Context) {
//        this.updateAll(context)
//    }

    private val lastUpdatedPreference = stringPreferencesKey("LastUpdateTimestamp")
    private val destinationKey = ActionParameters.Key<String>(
        "Hej"
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        Timber.d("Provide glance: $id")
        provideContent {
            MyContent(id)
        }
    }

    override suspend fun onDelete(context: Context, glanceId: GlanceId) {
        super.onDelete(context, glanceId)
    }

    @Composable
    fun MyContent(glanceId: GlanceId) {
        val context = LocalContext.current

        val viewModel = remember {
            EntryPoints.get(context, OagEntryPoint::class.java).vm()
        }

        val widgetState by viewModel.widgetState.collectAsState(initial = WidgetState.Loading)

//        val lastUpdated = currentState(lastUpdatedPreference)
//        Timber.d("Widget $glanceId state: ${widgetState} -- State thing: $lastUpdated")

        when (val state = widgetState) {
            WidgetState.Error -> {
                ErrorState {
                    actionStartActivity<WebsiteActivity>(actionParametersOf(destinationKey to "lel"))
                    viewModel.refresh()
                }
            }

            WidgetState.Loading -> LoadingState {
                viewModel.refresh()
            }

            is WidgetState.RateYesterday -> RateYesterdaysAlbum(
                coverUrl = state.coverUrl,
                goToWebsite = {
                    actionStartActivity<WebsiteActivity>(actionParametersOf(destinationKey to "lel"))
                    Timber.d("Go to website?")
                },
                glanceId
            )

            is WidgetState.TodaysAlbum -> TodaysAlbum(coverUrl = state.coverUrl, artist = state.artist, album = state.album) {
                viewModel.refresh()
            }
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
    fun TodaysAlbum(coverUrl: String, artist: String, album: String, refresh: () -> Unit) {
        val context = LocalContext.current
        var coverBitmap by remember { mutableStateOf<Bitmap?>(null) }

        LaunchedEffect(coverUrl) {
            coverBitmap = context.getImage(coverUrl)
        }
        Timber.d("TodaysAlbum: $coverUrl -- cover bitmap: $coverBitmap")

        coverBitmap?.let {
            Column(modifier = GlanceModifier.fillMaxSize()) {
                CoverImage(
                    modifier = GlanceModifier.fillMaxSize(),
                    bitmap = it,
                    onClick = refresh
                )
                Text(
                    text = "$artist - $album",
                    maxLines = 1,
                    style = TextStyle(
                        color = GlanceTheme.colors.onBackground,
                        fontSize = TextUnit(24f, TextUnitType.Sp),
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                    )
                )
            }
        } ?: Box(GlanceModifier.fillMaxSize().background(GlanceTheme.colors.background).clickable(refresh), contentAlignment = Alignment.Center) {
            Text(
                text = "No cover :/\n$artist - $album",
                style = TextStyle(
                    color = GlanceTheme.colors.onBackground,
                    fontSize = TextUnit(24f, TextUnitType.Sp),
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
            )
        }
    }


    @Composable
    fun RateYesterdaysAlbum(coverUrl: String, goToWebsite: () -> Unit, glanceId: GlanceId) {
        val context = LocalContext.current
        var coverBitmap by remember(coverUrl) { mutableStateOf<Bitmap?>(null) }

        LaunchedEffect(coverUrl) {
            coverBitmap = context.getImage(coverUrl)
        }
        coverBitmap?.let {
            CoverImage(
                modifier = GlanceModifier.fillMaxSize(),
                bitmap = it,
                tint = ColorProvider(GlanceTheme.colors.background.getColor(context).copy(alpha = 0.75f))
            )

            Box(
                modifier = GlanceModifier
                    .fillMaxSize().clickable(goToWebsite),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Unrated", modifier = GlanceModifier
                        .fillMaxWidth(),
                    style = TextStyle(
                        color = GlanceTheme.colors.onBackground,
                        fontSize = TextUnit(24f, TextUnitType.Sp),
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                    )
                )
            }
        }
    }

    @Composable
    fun CoverImage(modifier: GlanceModifier = GlanceModifier, bitmap: Bitmap, onClick: (() -> Unit)? = null, tint: ColorProvider? = null) {
        Image(
            provider = ImageProvider(bitmap),
            contentDescription = "Image from Picsum Photos",
            contentScale = ContentScale.Fit,
            colorFilter = tint?.let { ColorFilter.tint(it) },
            modifier = modifier
                .cornerRadius(16.dp)
                .apply {
                    if (onClick != null) {
                        clickable(onClick)
                    }
                }
        )
    }

    @Composable
    fun LoadingState(onClick: () -> Unit) {
        CircularProgressIndicator(modifier = GlanceModifier.clickable {
            onClick()
        })
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

//    fun Context.updateDailyAlbumWidget(id: GlanceId) {
//        val manager = GlanceAppWidgetManager(this)
//        val widget = DailyAlbumWidget()
//        val glanceIds = manager.getGlanceIds(widget.javaClass)
//        glanceIds.forEach { glanceId ->
//            widget.update(this@DailyAlbumWidget, glanceId)
//        }
//            DailyAlbumWidget().updateAll(this@updateDailyAlbumWidget)
//        }
//    }
}

@AndroidEntryPoint
class AlbumWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = DailyAlbumWidget()

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        Timber.i("GlanceAppWidgetReceiver: ${intent.extras?.keySet()?.joinToString { it }} ")
    }
}
