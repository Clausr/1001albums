package dk.clausr.a1001albumsgenerator.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dk.clausr.core.common.model.doOnFailure
import dk.clausr.core.common.model.doOnSuccess
import dk.clausr.core.data.repository.NotificationRepository
import dk.clausr.core.data.repository.OagRepository
import dk.clausr.core.data.repository.UserRepository
import dk.clausr.core.model.StreamingPlatform
import dk.clausr.core.network.NetworkError
import dk.clausr.core.ui.CoverData
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val oagRepository: OagRepository,
    private val userRepository: UserRepository,
    private val notificationRepository: NotificationRepository,
) : ViewModel() {
    private val _viewEffect = Channel<SettingsViewEffect>(Channel.BUFFERED)
    val viewEffect = _viewEffect.receiveAsFlow()

    val projectId: StateFlow<String?> = oagRepository.projectId.map { it }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null,
        )

    val streamingPlatform: StateFlow<StreamingPlatform?> = oagRepository.preferredStreamingPlatform.map { it }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null,
        )

    val coverData: StateFlow<CoverData> = oagRepository.albumCovers
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = CoverData.default(),
        )

    fun setProjectId(projectId: String) {
        Timber.d("setProjectId $projectId")
        viewModelScope.launch {
            val existingProject = oagRepository.project.firstOrNull()
            if (existingProject != null && existingProject.name.equals(projectId, ignoreCase = true)) {
                Timber.d("Same as existing project: ${existingProject.name}")
            } else {
                val asyncSetProject = async { oagRepository.setProject(projectId) }
                val asyncUpdateNotifications = async {
                    notificationRepository.updateNotifications(
                        origin = "SettingsVM",
                        projectId = projectId,
                        getRead = false,
                    )
                }

                asyncSetProject.await()
                    .doOnSuccess {
                        Timber.d("Set project success!")
                        asyncUpdateNotifications.await()
                        sendViewEffect(SettingsViewEffect.ProjectSet)
                    }
                    .doOnFailure { error ->
                        Timber.e(error.cause, "Could not change projectId")
                        sendViewEffect(SettingsViewEffect.Error(error = error))
                    }
            }
        }
    }

    fun setStreamingPlatform(streamingPlatform: StreamingPlatform) {
        viewModelScope.launch {
            oagRepository.setPreferredPlatform(streamingPlatform)
        }
    }

    fun markOnboardingAsCompleted() {
        viewModelScope.launch {
            userRepository.setOnboardingCompleted()
        }
    }

    private fun sendViewEffect(viewEffect: SettingsViewEffect) {
        viewModelScope.launch {
            _viewEffect.send(viewEffect)
        }
    }
}

sealed interface SettingsViewEffect {
    data class Error(val error: NetworkError) : SettingsViewEffect
    data object ProjectSet : SettingsViewEffect
}
