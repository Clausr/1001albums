package dk.clausr.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import dagger.hilt.android.AndroidEntryPoint
import dk.clausr.core.data_widget.AlbumWidgetDataDefinition
import dk.clausr.core.data_widget.SerializedWidgetState
import dk.clausr.core.model.StreamingLink
import dk.clausr.extensions.open1001Website
import dk.clausr.extensions.openSomeActivity
import dk.clausr.worker.BurstUpdateWorker
import dk.clausr.worker.SimplifiedWidgetWorker
import kotlinx.coroutines.delay
import timber.log.Timber

object SimplifiedAlbumWidget : GlanceAppWidget() {
    override val stateDefinition: GlanceStateDefinition<SerializedWidgetState> =
        AlbumWidgetDataDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // Load data needed to render the AppWidget.
        // Use `withContext` to switch to another thread for long running
        // operations.

        provideContent {
            GlanceTheme {
                AlbumWidget()
            }
        }
    }

    override fun onCompositionError(
        context: Context,
        glanceId: GlanceId,
        appWidgetId: Int,
        throwable: Throwable
    ) {
        Timber.e(throwable, "Composition error for glanceId: $glanceId")
        super.onCompositionError(context, glanceId, appWidgetId, throwable)
    }

    @Composable
    fun AlbumWidget() {
        val state = currentState<SerializedWidgetState>()
        val context = LocalContext.current

        Box(
            GlanceModifier.fillMaxSize()
        ) {
            when (val internalState = state) {
                is SerializedWidgetState.Error -> ErrorState()
                is SerializedWidgetState.Loading -> LoadingState()
                is SerializedWidgetState.NotInitialized -> {
                    Box(
                        GlanceModifier.fillMaxSize().background(
                            GlanceTheme.colors.background
                        ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = context.getString(R.string.state_not_configured),
                            style = TextStyle(
                                color = GlanceTheme.colors.onBackground,
                                fontSize = TextUnit(16f, TextUnitType.Sp),
                                fontWeight = FontWeight.Bold,
                            )
                        )
                    }
                }

                is SerializedWidgetState.Success -> {
                    var showLinks by remember {
                        mutableStateOf(false)
                    }

                    LaunchedEffect(showLinks) {
                        if (showLinks) {
                            delay(5000)
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
                            modifier = GlanceModifier.fillMaxWidth(),
                            coverUrl = internalState.data.coverUrl,
                        )

                        if (internalState.data.newAvailable) {
                            RatingNudge()
                        }

                        if (showLinks) {
                            LinkPill(internalState)
                        }
                    }
                }
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

    @Composable
    private fun LinkPill(
        internalState: SerializedWidgetState.Success
    ) {
        val context = LocalContext.current

        Row(
            GlanceModifier
                .background(GlanceTheme.colors.background)
                .padding(vertical = 8.dp, horizontal = 16.dp)
                .cornerRadius(100.dp)
        ) {
            Image(
                provider = ImageProvider(R.drawable.ic_wiki),
                contentDescription = "Wikipedia",
                colorFilter = ColorFilter.tint(GlanceTheme.colors.onBackground),
                modifier = GlanceModifier
                    .clickable {
                        context.openSomeActivity(internalState.data.wikiLink)
                    })

            internalState.data.streamingLinks.links.forEach { link ->
                StreamingService(streamingLink = link)
            }

            Spacer(GlanceModifier.width(16.dp))
            Image(
                provider = ImageProvider(R.drawable.ic_open_external),
                contentDescription = "Open website",
                colorFilter = ColorFilter.tint(GlanceTheme.colors.onBackground),
                modifier = GlanceModifier.clickable {
                    context.open1001Website(internalState.currentProjectId)

                    if (internalState.data.newAvailable) {
                        BurstUpdateWorker.enqueueBurstUpdate(context)
                    }
                })
        }
    }

    @Composable
    fun StreamingService(
        streamingLink: StreamingLink,
    ) {
        val context = LocalContext.current

        Spacer(GlanceModifier.width(16.dp))
        Image(
            provider = ImageProvider(R.drawable.ic_tidal),
            contentDescription = streamingLink.name,
            colorFilter = ColorFilter.tint(GlanceTheme.colors.onBackground),
//            modifier = GlanceModifier.clickable(onClick = actionRunCallback<RefreshAction>()),
            modifier = GlanceModifier.clickable {
                context.openSomeActivity(streamingLink.link)
            }
        )
    }
}

class RefreshAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        // do some work but offset long-term tasks (e.g a Worker)
        Timber.d("Refresh ting")
        SimplifiedAlbumWidget.update(context, glanceId)
    }
}

@AndroidEntryPoint
class SimplifiedAlbumWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = SimplifiedAlbumWidget

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        SimplifiedWidgetWorker.start(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        SimplifiedWidgetWorker.cancel(context)
    }
}
