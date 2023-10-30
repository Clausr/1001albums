package dk.clausr.widget

import dk.clausr.core.common.network.Dispatcher
import dk.clausr.core.common.network.OagDispatchers
import dk.clausr.core.data.repository.OagRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

class WidgetViewModel @Inject constructor(
    private val oagRepository: OagRepository,
    @Dispatcher(OagDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) {
    private val viewModelScope = CoroutineScope(ioDispatcher + SupervisorJob())
    private val refresh: MutableSharedFlow<Unit> = MutableSharedFlow(replay = 1)

    val widget = oagRepository.getWidgetFlow("decoid")

    private val widgetModel = combine(oagRepository.projectId, refresh) { projectId, _ ->
        projectId
    }
        .map { oagRepository.getWidget(it ?: "") }

    val widgetState: Flow<WidgetState> = combine(oagRepository.projectId, refresh) { projectId, _ ->
        projectId
    }
        .onStart { WidgetState.Loading }
        .mapNotNull { it }
        .map { projectId ->
            oagRepository.getWidget(projectId ?: "")
        }
        .flowOn(ioDispatcher)
        .map { widgetModel ->
            when {
                widgetModel == null -> WidgetState.Error
//                project.history.last().rating == Rating.Unrated -> WidgetState.RateYesterday(project.history.last().album.images.maxBy { it.height }.url)
                else -> {
//                    val currentAlbum = project.currentAlbum
                    WidgetState.TodaysAlbum(
                        coverUrl = widgetModel.currentCoverUrl,
                        artist = widgetModel.currentAlbumArtist,
                        album = widgetModel.currentAlbumTitle
                    )
                }
            }
        }


    init {
        viewModelScope.launch {
            refresh.emit(Unit)
        }
    }

    fun refresh() = CoroutineScope(ioDispatcher).launch {
        refresh.emit(Unit)
    }

}

sealed interface WidgetState {
    data object Loading : WidgetState
    data object Error : WidgetState
    data class RateYesterday(val coverUrl: String) : WidgetState
    data class TodaysAlbum(val coverUrl: String, val artist: String, val album: String) : WidgetState

}
