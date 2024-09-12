package dk.clausr.a1001albumsgenerator.onboarding

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
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class OnboardingScreenViewModel @Inject constructor(
    private val oagRepository: OagRepository,
    private val userRepository: UserRepository,
    private val notificationRepository: NotificationRepository,
) : ViewModel() {

    private val _viewEffect = Channel<IntroViewEffects>(Channel.BUFFERED)
    val viewEffect = _viewEffect.receiveAsFlow()

    private var _projectId: MutableStateFlow<String?> = MutableStateFlow(null)
    val projectId: Flow<String?> = _projectId
    private var _streamingPlatform: MutableStateFlow<StreamingPlatform?> = MutableStateFlow(null)
    val streamingPlatform: Flow<StreamingPlatform?> = _streamingPlatform

    fun setProjectId(projectId: String) {
        viewModelScope.launch {
            // If user goes back, don't query backend again
            val existingProject = oagRepository.project.firstOrNull()
            if (existingProject != null && existingProject.name.equals(projectId, ignoreCase = true)) {
                sendViewEffect(IntroViewEffects.ProjectSet)
            } else {
                oagRepository.setProject(projectId)
                    .doOnSuccess {
                        sendViewEffect(IntroViewEffects.ProjectSet)
                        this@OnboardingScreenViewModel._projectId.value = projectId
                    }
                    .doOnFailure { error ->
                        Timber.e(error.cause, "Could not set projectId")
                        sendViewEffect(IntroViewEffects.ProjectError(error = error))
                    }
            }
        }
    }

    fun setStreamingPlatform(streamingPlatform: StreamingPlatform) {
        viewModelScope.launch {
            this@OnboardingScreenViewModel._streamingPlatform.value = streamingPlatform
            oagRepository.setPreferredPlatform(streamingPlatform)

            markIntroFlowAsCompleted()
        }
    }

    fun markIntroFlowAsCompleted() {
        viewModelScope.launch {
            userRepository.setOnboardingCompleted()
            sendViewEffect(IntroViewEffects.OnboardingDone)
        }
    }

    private fun sendViewEffect(viewEffect: IntroViewEffects) {
        viewModelScope.launch {
            _viewEffect.send(viewEffect)
        }
    }
}

sealed interface IntroViewEffects {
    data class ProjectError(val error: NetworkError) : IntroViewEffects
    data object ProjectSet : IntroViewEffects
    data object OnboardingDone : IntroViewEffects
}
