package dk.clausr.a1001albumsgenerator.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dk.clausr.core.common.model.doOnFailure
import dk.clausr.core.common.model.doOnSuccess
import dk.clausr.core.data.repository.OagRepository
import dk.clausr.core.data.repository.UserRepository
import dk.clausr.core.model.StreamingPlatform
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val oagRepository: OagRepository,
    private val userRepository: UserRepository,
) : ViewModel() {

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

    fun setProjectId(projectId: String) {
        viewModelScope.launch {
            // If user goes back, don't query backend again
            val existingProject = oagRepository.project.firstOrNull()
            if (existingProject != null && existingProject.name.equals(projectId, ignoreCase = true)) {
//                sendViewEffect(IntroViewEffects.ProjectSet)
            } else {
                oagRepository.setProject(projectId)
                    .doOnSuccess {
//                        sendViewEffect(IntroViewEffects.ProjectSet)
                    }
                    .doOnFailure { error ->
//                        Timber.e(error.cause, "Some error..")
//                        sendViewEffect(IntroViewEffects.ProjectNotFound)
                    }
            }
        }
    }

    fun setStreamingPlatform(streamingPlatform: StreamingPlatform) {
        viewModelScope.launch {
            oagRepository.setPreferredPlatform(streamingPlatform)
//            sendViewEffect(IntroViewEffects.StreamingServiceSet)
        }
    }

    fun markOnboardingAsCompleted() {
        viewModelScope.launch {
            userRepository.setOnboardingCompleted()
        }
    }
}

sealed interface SettingsViewEffect {
    data object ProjectSet : SettingsViewEffect
    data class ProjectNotSet(val message: String) : SettingsViewEffect
    data object StreamingPlatformSet : SettingsViewEffect
}
