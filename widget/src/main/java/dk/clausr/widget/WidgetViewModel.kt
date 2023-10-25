package dk.clausr.widget

import dk.clausr.core.common.network.Dispatcher
import dk.clausr.core.common.network.OagDispatchers
import dk.clausr.core.data.repository.OagRepository
import dk.clausr.core.model.Project
import dk.clausr.core.model.Rating
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

class WidgetViewModel @Inject constructor(
    private val oagRepository: OagRepository,
    @Dispatcher(OagDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) {
    val viewModelScope = CoroutineScope(ioDispatcher + SupervisorJob())
    private val refresh: MutableSharedFlow<Unit> = MutableSharedFlow(replay = 1)

    private val project: Flow<Project?> = combine(oagRepository.projectId, refresh) { projectId, _ ->
        projectId
    }
        .distinctUntilChanged()
        .map {
            if (it?.isNotBlank() == true) {
                oagRepository.getProject(it).first()
            } else null
        }

    val widgetState: Flow<WidgetState> = combine(oagRepository.projectId, refresh) { projectId, _ ->
        projectId
    }
        .onStart { WidgetState.Loading }
        .map { projectId ->
            if (projectId?.isNotBlank() == true) {
                oagRepository.getProject(projectId).first()
            } else null
        }
        .map { project ->
            when {
                project == null -> WidgetState.Error
                project.history.last().rating == Rating.Unrated -> WidgetState.RateYesterday(project.history.last().album.images.maxBy { it.height }.url)
                else -> {
                    val currentAlbum = project.currentAlbum
                    WidgetState.TodaysAlbum(
                        coverUrl = currentAlbum.images.maxBy { it.height }.url,
                        artist = currentAlbum.artist,
                        album = currentAlbum.name
                    )
                }
            }
        }


    init {
        viewModelScope.launch {
            refresh.emit(Unit)
        }
    }

//    @WorkerThread
//    fun getGroup(groupId: String = "claus-rasmus-delemusik"): Flow<Group> {
//        Timber.d("Get group: $groupId")
//        return oagRepository.getGroup(groupId).flowOn(ioDispatcher)
//    }

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
