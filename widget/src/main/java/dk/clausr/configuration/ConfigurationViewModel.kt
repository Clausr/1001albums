package dk.clausr.configuration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dk.clausr.core.data.repository.OagRepository
import dk.clausr.core.model.Project
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConfigurationViewModel @Inject constructor(
    private val oagRepository: OagRepository,
) : ViewModel() {

    private val _projectId: Flow<String?> = oagRepository.projectId.stateIn(viewModelScope, started = SharingStarted.WhileSubscribed(), null)

    val widget = _projectId.mapNotNull { it }
        .flatMapLatest { oagRepository.getWidgetFlow(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = null
        )

    val project: StateFlow<Project?> = _projectId
        .mapNotNull { it }
        .flatMapLatest { oagRepository.getProjectFlow(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = null
        )

    fun setProjectId(projectId: String) = viewModelScope.launch {
        oagRepository.setProject(projectId)
    }
}
