package dk.clausr.configuration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dk.clausr.core.data.repository.OagRepository
import dk.clausr.core.model.Group
import dk.clausr.core.model.Project
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConfigurationViewModel @Inject constructor(
    private val oagRepository: OagRepository,
) : ViewModel() {
    private val _groupId: MutableStateFlow<String?> = MutableStateFlow(null)
    private val _projectId: MutableStateFlow<String?> = MutableStateFlow(null)

    val groupFlow: StateFlow<Group?> = combine(_groupId, oagRepository.groupId) { explicitGroupId, preferences ->
        explicitGroupId ?: preferences
    }
        .mapNotNull { it }
        .flatMapLatest {
            oagRepository.getGroup(it)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = null
        )

    val project: StateFlow<Project?> = combine(_projectId, oagRepository.projectId) { explicitProjectId, preferences ->
        explicitProjectId ?: preferences
    }
        .mapNotNull { it }
        .distinctUntilChanged()
        .flatMapLatest { oagRepository.getProject(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = null
        )

    fun setGroupId(groupId: String) = viewModelScope.launch {
        _groupId.emit(groupId)
    }

    fun setProjectId(projectId: String) = viewModelScope.launch {
        _projectId.emit(projectId)
    }
}
