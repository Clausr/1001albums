package dk.clausr.a1001albumsgenerator.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dk.clausr.core.common.model.doOnFailure
import dk.clausr.core.common.model.doOnSuccess
import dk.clausr.core.data.repository.OagRepository
import dk.clausr.core.model.StreamingPlatform
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class OnboardingScreenViewModel @Inject constructor(
    private val oagRepository: OagRepository,
) : ViewModel() {

    private val _viewEffect = Channel<IntroViewEffects>(Channel.BUFFERED)
    val viewEffect = _viewEffect.receiveAsFlow()

    private var projectId: MutableStateFlow<String?> = MutableStateFlow(null)
    private var streamingPlatform: MutableStateFlow<StreamingPlatform?> = MutableStateFlow(null)
    private var introFlowCompleted: MutableStateFlow<Boolean> = MutableStateFlow(false)

    private fun commitSettings() = viewModelScope.launch {
        val projectId = projectId.value
        val streamingPlatform = streamingPlatform.value
        require(projectId != null)
        require(streamingPlatform != null)

        oagRepository.setPreferredPlatform(streamingPlatform)
        oagRepository.setProject(projectId)
            .doOnSuccess {
                Timber.i("Project found! All good -> Continue with ${it.name}")
            }
            .doOnFailure { message, throwable ->
                Timber.e(throwable, "Could not set project.")
                _viewEffect.send(IntroViewEffects.ProjectNotFound)
            }
    }

    val viewState = combine(
        projectId,
        streamingPlatform,
        introFlowCompleted,
    ) { id, platform, introCompleted ->
        if (introCompleted) {
            require(id != null)
            require(platform != null)
            IntroViewState.Done(id, platform)
        } else if (id != null && platform != null) {
            IntroViewState.StreamingServiceSet(id, platform)
        } else if (id != null) {
            IntroViewState.ProjectSet(id)
        } else {
            IntroViewState.Initial
        }
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
            initialValue = IntroViewState.Initial,
        )

    fun setProjectId(projectId: String) {
        this.projectId.value = projectId
    }

    fun setStreamingPlatform(streamingPlatform: StreamingPlatform) {
        this.streamingPlatform.value = streamingPlatform
    }

    fun markIntroFlowAsCompleted() {
        introFlowCompleted.value = true
        commitSettings()
    }
}

sealed interface IntroViewEffects {
    data object ProjectNotFound : IntroViewEffects
}

sealed interface IntroViewState {
    data object Initial : IntroViewState
    data class ProjectSet(val projectId: String) : IntroViewState
    data class StreamingServiceSet(val projectId: String, val streamingPlatform: StreamingPlatform) : IntroViewState
    data class Done(val projectId: String, val streamingPlatform: StreamingPlatform) : IntroViewState
}
