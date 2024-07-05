package dk.clausr.feature.overview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dk.clausr.core.data.repository.OagRepository
import dk.clausr.core.model.Album
import dk.clausr.core.model.Project
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class OverviewViewModel @Inject constructor(
    oagRepository: OagRepository
) : ViewModel() {

    val uiState = combine(
        oagRepository.project,
        oagRepository.currentAlbum,
        oagRepository.historicAlbums,
    ) { project, currentAlbum, albums ->
        if (project != null) {
            OverviewUiState.Success(
                project = project,
                currentAlbum = currentAlbum,
            )
        } else {
            OverviewUiState.Error
        }
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = OverviewUiState.Loading
        )

    init {
        viewModelScope.launch {
            Timber.d("Latest album rated? ${oagRepository.isLatestAlbumRated()}")
        }
    }
}

sealed interface OverviewUiState {
    data object Loading : OverviewUiState
    data class Success(
        val project: Project,
        val currentAlbum: Album?,
    ) : OverviewUiState

    data object Error : OverviewUiState
}
