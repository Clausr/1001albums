package dk.clausr.feature.overview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dk.clausr.core.data.repository.OagRepository
import dk.clausr.core.model.HistoricAlbum
import dk.clausr.core.model.Project
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class OverviewViewModel @Inject constructor(
    oagRepository: OagRepository
) : ViewModel() {

    val uiState = oagRepository.project.map { project ->
        if (project != null) {
            OverviewUiState.Success(project = project, albums = project.history.reversed())
        } else {
            OverviewUiState.Error
        }
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = OverviewUiState.Loading
        )
}

sealed interface OverviewUiState {
    data object Loading : OverviewUiState
    data class Success(val project: Project, val albums: List<HistoricAlbum>) : OverviewUiState
    data object Error : OverviewUiState
}
