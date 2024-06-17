package dk.clausr.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import dk.clausr.core.data.repository.OagRepository
import dk.clausr.core.data_widget.SerializedWidgetState
import dk.clausr.core.data_widget.SerializedWidgetState.Companion.projectUrl
import dk.clausr.worker.SimplifiedWidgetWorker
import kotlinx.coroutines.delay
import timber.log.Timber

// Periodic update thing:
// https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:glance/glance-appwidget/samples/src/main/java/androidx/glance/appwidget/samples/GlanceAppWidgetSamples.kt;drc=c28b42063433bb0f928a897c0d6ec31b45ba2021;l=114
class AlbumCoverWidget2 : GlanceAppWidget() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface AlbumCoverWidgetEntryPoint {
        fun oagRepository(): OagRepository
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val appContext = context.applicationContext
        val hiltEntryPoint =
            EntryPointAccessors.fromApplication(appContext, AlbumCoverWidgetEntryPoint::class.java)

        val repo = hiltEntryPoint.oagRepository()

        Timber.d("repo.... ${repo.widgetState}")
        provideContent {
            val state: SerializedWidgetState by repo.widgetState.collectAsState(initial = SerializedWidgetState.NotInitialized)
            GlanceTheme {
                Content(state = state)
            }
        }
    }

}

@Composable
fun Content(
    state: SerializedWidgetState,
) {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
//            .background(GlanceTheme.colors.widgetBackground)
    ) {
        when (state) {
            is SerializedWidgetState.Loading -> {
                LoadingState()
            }

            is SerializedWidgetState.Success -> {
                ShowAlbumCover(state)
            }

            is SerializedWidgetState.Error -> {
                ErrorState()
            }

            SerializedWidgetState.NotInitialized -> {
                Box(
                    GlanceModifier.fillMaxSize().background(
                        GlanceTheme.colors.background
                    ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = LocalContext.current.getString(R.string.state_not_configured),
                        style = TextStyle(
                            color = GlanceTheme.colors.onBackground,
                            fontSize = TextUnit(16f, TextUnitType.Sp),
                            fontWeight = FontWeight.Bold,
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun ShowAlbumCover(
    state: SerializedWidgetState.Success,
) {
    var showLinks by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(showLinks) {
        if (showLinks) {
            delay(timeMillis = 5_000)
            showLinks = false
        }
    }

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .clickable {
                showLinks = !showLinks
            },
        contentAlignment = Alignment.BottomCenter,
    ) {

        AlbumCover(
            modifier = GlanceModifier.fillMaxSize(),
            coverUrl = state.data.coverUrl
        )

        if (state.data.newAvailable) {
            RatingNudge()
        }

        if (showLinks) {
            LinkPill(
                wikipediaLink = state.data.wikiLink,
                streamingLinks = state.data.streamingLinks,
                projectUrl = state.projectUrl ?: ""
            )
        }
    }
}

@Composable
private fun RatingNudge() {
    val context = LocalContext.current

    Box(
        GlanceModifier
            .fillMaxSize()
            .background(
                GlanceTheme.colors.widgetBackground.getColor(context)
                    .copy(alpha = 0.75f)
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = LocalContext.current.getString(R.string.state_new_available),
            modifier = GlanceModifier.fillMaxWidth(),
            style = TextStyle(
                color = GlanceTheme.colors.onBackground,
                fontSize = TextUnit(16f, TextUnitType.Sp),
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
        )
    }
}

@AndroidEntryPoint
class AlbumCoverWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget
        get() = AlbumCoverWidget2()

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        SimplifiedWidgetWorker.start(context)

    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        SimplifiedWidgetWorker.cancel(context)
    }

}