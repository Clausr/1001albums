package dk.clausr.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
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
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import dagger.hilt.android.AndroidEntryPoint
import dk.clausr.data.AlbumWidgetDataDefinition
import dk.clausr.data.SerializedWidgetState
import dk.clausr.extensions.openWebsite
import dk.clausr.widget.R.string
import dk.clausr.worker.SimplifiedWidgetWorker

object SimplifiedAlbumWidget : GlanceAppWidget() {
    override val stateDefinition: GlanceStateDefinition<SerializedWidgetState> =
        AlbumWidgetDataDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) = provideContent {
        GlanceTheme {
            AlbumWidget()
        }
    }

    @Composable
    fun AlbumWidget() {
        val state = currentState<SerializedWidgetState>()
        val context = LocalContext.current

        Box(
            GlanceModifier.fillMaxSize()
        ) {
            when (state) {
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
                            text = stringResource(id = string.state_not_configured),
                            style = TextStyle(
                                color = GlanceTheme.colors.onBackground,
                                fontSize = TextUnit(16f, TextUnitType.Sp),
                                fontWeight = FontWeight.Bold,
                            )
                        )
                    }
                }

                is SerializedWidgetState.Success -> {
                    DailyAlbumWidget.CoverImage(
                        modifier = GlanceModifier.fillMaxSize().clickable {
                            state.projectId?.let { context.openWebsite(it) }
                        },
                        coverUrl = state.data.coverUrl,
                    )

                    if (state.data.newAvailable) {
                        Box(
                            GlanceModifier.fillMaxSize().background(
                                GlanceTheme.colors.background.getColor(context).copy(alpha = 0.75f)
                            )
                        ) {
                            Text(
                                text = stringResource(id = string.state_new_available),
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
                }
            }
        }
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
