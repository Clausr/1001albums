package dk.clausr.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.glance.Button
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.components.CircleIconButton
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import dk.clausr.core.common.extensions.openProject
import dk.clausr.core.data.repository.NotificationRepository
import dk.clausr.core.data.repository.OagRepository
import dk.clausr.core.data_widget.AlbumWidgetDataDefinition
import dk.clausr.core.data_widget.SerializedWidgetState
import dk.clausr.core.data_widget.SerializedWidgetState.Companion.projectUrl
import dk.clausr.core.model.Notification
import dk.clausr.worker.BurstUpdateWorker
import dk.clausr.worker.SimplifiedWidgetWorker
import kotlinx.coroutines.delay
import timber.log.Timber

// Periodic update thing:
// https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:glance/glance-appwidget/samples/src/main/java/androidx/glance/appwidget/samples/GlanceAppWidgetSamples.kt;drc=c28b42063433bb0f928a897c0d6ec31b45ba2021;l=114
class AlbumCoverWidget : GlanceAppWidget() {
    override val stateDefinition: GlanceStateDefinition<SerializedWidgetState> =
        AlbumWidgetDataDefinition

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface AlbumCoverWidgetEntryPoint {
        fun oagRepository(): OagRepository
        fun notificationRepository(): NotificationRepository
    }

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) {
        Timber.d("GlanceID: $id")
        val appContext = context.applicationContext
        val hiltEntryPoint =
            EntryPointAccessors.fromApplication(appContext, AlbumCoverWidgetEntryPoint::class.java)

        val repo = hiltEntryPoint.oagRepository()
        val notificationRepo = hiltEntryPoint.notificationRepository()

        provideContent {
            val currentState = currentState<SerializedWidgetState>()

            val state: SerializedWidgetState by repo.widgetState
                .collectAsState(initial = currentState)

            val notifications: List<Notification> by notificationRepo.unreadNotifications.collectAsState(emptyList())

            Timber.d("Notifications: ${notifications.size}")
            GlanceTheme {
                Content(
                    state = state,
                    notificationCount = notifications.size,
                )
            }
        }
    }
}

@Composable
fun Content(
    state: SerializedWidgetState,
    notificationCount: Int,
    modifier: GlanceModifier = GlanceModifier,
) {
    Timber.d("Widget state: $state")
    Box(
        modifier = modifier
            .fillMaxSize(),
    ) {
        when (state) {
            is SerializedWidgetState.Loading -> {
//                LoadingState()
            }

            is SerializedWidgetState.Success -> {
                ShowAlbumCover(state = state, notificationCount = notificationCount)
            }

            is SerializedWidgetState.Error -> {
                ErrorState()
            }

            SerializedWidgetState.NotInitialized -> {
                Box(
                    GlanceModifier
                        .fillMaxSize()
                        .background(GlanceTheme.colors.background),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = LocalContext.current.getString(R.string.state_not_configured),
                        style = TextStyle(
                            color = GlanceTheme.colors.onBackground,
                            fontSize = TextUnit(value = 16f, TextUnitType.Sp),
                            fontWeight = FontWeight.Bold,
                        ),
                    )
                }
            }
        }
    }
}

@Composable
private fun ShowAlbumCover(
    state: SerializedWidgetState.Success,
    notificationCount: Int,
    modifier: GlanceModifier = GlanceModifier,
) {
    val context = LocalContext.current
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
        modifier = modifier
            .fillMaxSize()
            .clickable {
                showLinks = !showLinks
            },
        contentAlignment = Alignment.BottomCenter,
    ) {
        AlbumCover(
            modifier = GlanceModifier.fillMaxSize(),
            coverUrl = state.data.coverUrl,
        )

        if (state.data.newAvailable) {
            RatingNudge(state.currentProjectId)
        }

        if (notificationCount > 0) {
            val intent = Intent(context, Class.forName("dk.clausr.a1001albumsgenerator.MainActivity")).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val actionIntent = actionStartActivity(intent)
            CircleIconButton(
                modifier = GlanceModifier.size(36.dp),
                imageProvider = ImageProvider(R.drawable.ic_notification_active),
                contentDescription = null,
                onClick = actionIntent,
            )
        }

        if (showLinks) {
            LinkPill(
                wikipediaLink = state.data.wikiLink,
                streamingServices = state.data.streamingServices,
                preferredStreamingPlatform = state.data.preferredStreamingPlatform,
                projectUrl = state.projectUrl ?: "",
            )
        }
    }
}

@Composable
private fun RatingNudge(
    projectId: String,
    modifier: GlanceModifier = GlanceModifier,
) {
    val context = LocalContext.current

    Column(
        modifier
            .fillMaxSize()
            .background(
                GlanceTheme.colors.widgetBackground.getColor(context)
                    .copy(alpha = 0.75f),
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = LocalContext.current.getString(R.string.state_new_available),
            modifier = GlanceModifier.fillMaxWidth(),
            style = TextStyle(
                color = GlanceTheme.colors.onBackground,
                fontSize = TextUnit(value = 16f, TextUnitType.Sp),
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            ),
        )
        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            for (stars in 1..5) {
                if (stars != 1) Spacer(GlanceModifier.width(8.dp))
                val icon =
                    if (stars == 1) R.drawable.baseline_star_24 else R.drawable.baseline_star_border_24
                CircleIconButton(
                    modifier = GlanceModifier.size(36.dp),
                    imageProvider = ImageProvider(icon),
                    contentDescription = null,
                    onClick = {
                        context.openProject(projectId, stars)
                        // Start requesting for changes
                        BurstUpdateWorker.enqueueUnique(context, projectId = projectId)
                    },
                )
            }
        }
        Row(
            GlanceModifier.fillMaxWidth().padding(top = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Button(
                text = stringResource(R.string.nudge_did_not_listen_button_title),
                onClick = {
                    context.openProject(projectId)
                    // Start requesting for changes
                    BurstUpdateWorker.enqueueUnique(context, projectId = projectId)
                },
            )
        }
    }
}

@AndroidEntryPoint
class AlbumCoverWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget
        get() = AlbumCoverWidget()

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        SimplifiedWidgetWorker.start(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        SimplifiedWidgetWorker.cancel(context)
    }
}
