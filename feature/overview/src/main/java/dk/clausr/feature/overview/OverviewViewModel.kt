package dk.clausr.feature.overview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dk.clausr.core.common.extensions.formatMonthAndYear
import dk.clausr.core.common.extensions.toLocalDateTime
import dk.clausr.core.data.repository.NotificationRepository
import dk.clausr.core.data.repository.OagRepository
import dk.clausr.core.data_widget.SerializedWidgetState
import dk.clausr.core.model.Album
import dk.clausr.core.model.HistoricAlbum
import dk.clausr.core.model.Project
import dk.clausr.core.model.Rating
import dk.clausr.core.model.StreamingPlatform
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class OverviewViewModel @Inject constructor(
    oagRepository: OagRepository,
    private val notificationsRepository: NotificationRepository,
) : ViewModel() {
    var projectId = MutableStateFlow("")

    val notifications = notificationsRepository.unreadNotifications
        .map { it.toPersistentList() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = persistentListOf(),
        )

    val uiState = combine(
        oagRepository.project,
        oagRepository.currentAlbum,
        oagRepository.widgetState,
        oagRepository.preferredStreamingPlatform,
    ) { project, currentAlbum, widgetState, platform ->
        if (project != null) {
            OverviewUiState.Success(
                project = project,
                currentAlbum = currentAlbum,
                widgetState = widgetState,
                didNotListen = project.didNotListenAlbums(),
                topRated = project.topRatedAlbums(),
                streamingPlatform = platform,
                groupedHistory = project.groupedHistory(),
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
        return historicAlbums.filter { it.rating == Rating.Rated(rating = 5) }.toImmutableList()
    }

    private fun Project.didNotListenAlbums(): ImmutableList<HistoricAlbum> {
        return historicAlbums.filter { it.rating !is Rating.Rated }.toImmutableList()
    }

    private fun Project.groupedHistory(): Map<String, List<HistoricAlbum>> {
        return historicAlbums.groupBy {
            val generated = it.generatedAt.toLocalDateTime()
            val date = LocalDate.of(generated.year, generated.monthValue, 1)
            date.formatMonthAndYear().replaceFirstChar { it.uppercase() }
        }
    }

    fun readAllNotifications() = viewModelScope.launch {
        notificationsRepository.readAll(projectId = projectId.value)
    }

    init {
        viewModelScope.launch {
            oagRepository.project.collectLatest {

                it?.name?.let {
                    notificationsRepository.updateNotifications(it)
                    projectId.value = it
                }
            }
        }
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
        val groupedHistory: Map<String, List<HistoricAlbum>>,
    ) : OverviewUiState

    data object Error : OverviewUiState
}
