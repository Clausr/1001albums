package dk.clausr.a1001albumsgenerator

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dk.clausr.core.data.repository.OagRepository
import dk.clausr.core.model.Project
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    oagRepository: OagRepository,
) : ViewModel() {

    val uiState = oagRepository.project.map {
        MainViewState.HasProject(it)
    }
}

sealed interface MainViewState {
    data object Loading : MainViewState
    data class HasProject(val project: Project?) : MainViewState
}
