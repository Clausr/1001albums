package dk.clausr.feature.overview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dk.clausr.core.data.repository.OagRepository
import dk.clausr.core.model.Album
import dk.clausr.core.model.Project
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OverviewViewModel @Inject constructor(
    oagRepository: OagRepository
) : ViewModel() {

    val projectId = oagRepository.projectId
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    val uiState = combine(oagRepository.project, oagRepository.albums) { project, albums ->
        if (project != null) {
            OverviewUiState.Success(project, albums)
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
        viewModelScope.launch(Dispatchers.IO) {
            projectId.collectLatest {
                it?.let { oagRepository.setProject(it) }
            }
        }
    }
}

sealed interface OverviewUiState {
    data object Loading : OverviewUiState
    data class Success(val project: Project, val albums: List<Album>) : OverviewUiState
    data object Error : OverviewUiState
}
