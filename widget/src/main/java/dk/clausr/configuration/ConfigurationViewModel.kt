package dk.clausr.configuration

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dk.clausr.core.data.repository.OagRepository
import dk.clausr.core.data_widget.SerializedWidgetState
import dk.clausr.core.model.StreamingPlatform
import dk.clausr.widget.AlbumCoverWidget
import dk.clausr.worker.SimplifiedWidgetWorker
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ConfigurationViewModel @Inject constructor(
    private val oagRepository: OagRepository,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    val widgetState = oagRepository.widgetState.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SerializedWidgetState.NotInitialized
    )

    fun setProjectId(projectId: String) = viewModelScope.launch {
        Timber.d("Config VM : setProjectId $projectId")
        oagRepository.setProject(projectId)
        SimplifiedWidgetWorker.start(context)
    }

    fun updateWidgets() = viewModelScope.launch {
        AlbumCoverWidget().updateAll(context)
    }

    fun setPreferredStreamingPlatform(platform: StreamingPlatform) = viewModelScope.launch {
        oagRepository.setPreferredPlatform(platform)
    }
}
