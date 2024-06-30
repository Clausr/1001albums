package dk.clausr.a1001albumsgenerator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dk.clausr.core.data.repository.OagRepository
import dk.clausr.core.model.Project
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    oagRepository: OagRepository,
) : ViewModel() {

    val uiState = oagRepository.project.map {
        if (it != null) {
            MainViewState.HasProject(it)
        } else {
            MainViewState.NoProject
        }
    }
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = MainViewState.Loading
        )
}

sealed interface MainViewState {
    data object Loading : MainViewState
    data object NoProject : MainViewState
    data class HasProject(val project: Project) : MainViewState
}