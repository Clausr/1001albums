package dk.clausr.configuration

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dk.clausr.core.data.repository.OagRepository
import dk.clausr.core.data_widget.SerializedWidgetState
import dk.clausr.core.model.Project
import dk.clausr.widget.SimplifiedAlbumWidget
import dk.clausr.worker.SimplifiedWidgetWorker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConfigurationViewModel @Inject constructor(
    private val oagRepository: OagRepository,
    @ApplicationContext private val context: Context,
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
        val newProject = oagRepository.setProject(projectId)
        mutableProject.emit(newProject)
        if (newProject != null) {
            SimplifiedWidgetWorker.start(context)
        }
    }

    fun finish() = viewModelScope.launch {
        SimplifiedAlbumWidget.updateAll(context)
    }
}
