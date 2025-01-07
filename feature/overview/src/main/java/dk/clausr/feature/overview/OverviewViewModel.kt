package dk.clausr.feature.overview

import android.content.ActivityNotFoundException
import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.updateAll
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dk.clausr.core.common.extensions.formatMonthAndYear
import dk.clausr.core.common.extensions.openLink
import dk.clausr.core.common.extensions.toLocalDateTime
import dk.clausr.core.common.model.doOnFailure
import dk.clausr.core.common.model.doOnSuccess
import dk.clausr.core.data.repository.NotificationRepository
import dk.clausr.core.data.repository.OagRepository
import dk.clausr.core.data_widget.SerializedWidgetState
import dk.clausr.core.model.Album
import dk.clausr.core.model.HistoricAlbum
import dk.clausr.core.model.Notification
import dk.clausr.core.model.Project
import dk.clausr.core.model.Rating
import dk.clausr.core.model.StreamingPlatform
import dk.clausr.widget.AlbumCoverWidget
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class OverviewViewModel @Inject constructor(
    private val oagRepository: OagRepository,
    @ApplicationContext private val context: Context,
    private val notificationsRepository: NotificationRepository,
) : ViewModel() {
    private var projectId = MutableStateFlow("")
    private val _viewEffect = Channel<ViewEffect>(Channel.BUFFERED)
    val viewEffect = _viewEffect.receiveAsFlow()

    private val _unreadNotifications = notificationsRepository.unreadNotifications
        .map { it.toPersistentList() }

    // Default to true, so it only shows later
    private val _isUsingWidget = MutableStateFlow(true)

    val uiState = dk.clausr.core.common.extensions.combine(
        oagRepository.project,
        oagRepository.currentAlbum,
        oagRepository.widgetState,
        oagRepository.preferredStreamingPlatform,
        _unreadNotifications,
        _isUsingWidget,
    ) {
            project: Project?,
            currentAlbum: Album?,
            widgetState: SerializedWidgetState,
            platform: StreamingPlatform,
            unreadNotifications: ImmutableList<Notification>,
            isUsingWidget: Boolean,
        ->
        if (project != null) {
            OverviewUiState.Success(
                project = project,
                currentAlbum = currentAlbum,
                widgetState = widgetState,
                didNotListen = project.didNotListenAlbums(),
                topRated = project.topRatedAlbums(),
                streamingPlatform = platform,
                groupedHistory = project.groupedHistory().toImmutableMap(),
                notifications = unreadNotifications,
                isUsingWidget = isUsingWidget,
            )
        } else {
            OverviewUiState.Error
        }
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = OverviewUiState.Loading,
        )

    private fun Project.topRatedAlbums(): ImmutableList<HistoricAlbum> {
        return historicAlbums.filter { it.metadata?.rating == Rating.Rated(rating = 5) }.toImmutableList()
    }

    private fun Project.didNotListenAlbums(): ImmutableList<HistoricAlbum> {
        return historicAlbums.filter { it.metadata != null && it.metadata?.rating !is Rating.Rated }.toImmutableList()
    }

    private fun Project.groupedHistory(): Map<String, List<HistoricAlbum>> {
        return historicAlbums.groupBy {
            val generated = it.metadata?.generatedAt?.toLocalDateTime()
            val date = LocalDate.of(generated?.year ?: 1970, generated?.monthValue ?: 1, 1)
            date.formatMonthAndYear().replaceFirstChar { it.uppercase() }
        }
    }

    fun clearUnreadNotifications() {
        viewModelScope.launch {
            notificationsRepository.readAll(projectId.value)
                .doOnSuccess {
                    Timber.d("Notifications marked as read, update widget.")
                    AlbumCoverWidget().updateAll(context = context)
                }
                .doOnFailure {
                    Timber.e(it.cause, "Could not read all notifications")
                }
        }
    }

    fun openStreamingLink(url: String) {
        try {
            context.openLink(url)
        } catch (e: ActivityNotFoundException) {
            Timber.e(e, "Couldn't open streaming link")

            viewModelScope.launch {
                _viewEffect.send(ViewEffect.ShowSnackbar(message = "Could not find any browser to handle the link"))
            }
        }
    }

    init {
        viewModelScope.launch {
            oagRepository.project.collectLatest { project ->
                Timber.d("Project updated ${project?.name}")
                project?.name?.let { projectId ->
                    notificationsRepository.updateNotifications(origin = "OverviewViewModel", projectId = projectId)
                    this@OverviewViewModel.projectId.value = projectId
                }
            }
        }

        updateIsUsingWidget()
    }

    private fun updateIsUsingWidget() {
        viewModelScope.launch {
            val widgets = GlanceAppWidgetManager(context).getGlanceIds(AlbumCoverWidget::class.java)
            Timber.d("Widgets count: ${widgets.size}")
            _isUsingWidget.emit(widgets.isNotEmpty())
        }
    }

    sealed interface ViewEffect {
        data class ShowSnackbar(val message: String) : ViewEffect
    }
}

sealed interface OverviewUiState {
    data object Loading : OverviewUiState
    data class Success(
        val project: Project,
        val didNotListen: ImmutableList<HistoricAlbum>,
        val currentAlbum: Album?,
        val widgetState: SerializedWidgetState,
        val topRated: ImmutableList<HistoricAlbum>,
        val streamingPlatform: StreamingPlatform,
        val groupedHistory: ImmutableMap<String, List<HistoricAlbum>>,
        val notifications: ImmutableList<Notification>,
        val isUsingWidget: Boolean,
    ) : OverviewUiState

    data object Error : OverviewUiState
}
