package dk.clausr.configuration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dk.clausr.core.data.repository.OagRepository
import dk.clausr.core.data_widget.SerializedWidgetState
import dk.clausr.core.model.Project
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConfigurationViewModel @Inject constructor(
    private val oagRepository: OagRepository,
) : ViewModel() {

    val widgetState = oagRepository.widgetState.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SerializedWidgetState.Loading(null)
    )

    private val mutableProject = MutableStateFlow<Project?>(null)
    val project: StateFlow<Project?> = mutableProject.stateIn(
        scope = viewModelScope, started = SharingStarted.Lazily, initialValue = null
    )

    fun setProjectId(projectId: String) = viewModelScope.launch {
        mutableProject.emit(oagRepository.setProject(projectId))
    }
}
