package dk.clausr.a1001albumsgenerator.onboarding.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dk.clausr.core.common.model.doOnFailure
import dk.clausr.core.common.model.doOnSuccess
import dk.clausr.core.data.repository.OagRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ProjectViewModel @Inject constructor(
    private val oagRepository: OagRepository,
) : ViewModel() {

    private val _viewEffect = Channel<ProjectViewEffects>(Channel.BUFFERED)
    val viewEffect = _viewEffect.receiveAsFlow()

    fun setProjectId(projectId: String) = viewModelScope.launch {
        oagRepository.setProject(projectId)
            .doOnSuccess {
                Timber.i("Project found! All good -> Continue with ${it.name}")
            }
            .doOnFailure { message, throwable ->
                Timber.e(throwable, "Could not set project.")
                _viewEffect.send(ProjectViewEffects.ProjectNotFound)
            }
    }
}

sealed interface ProjectViewEffects {
    data object ProjectNotFound : ProjectViewEffects
}

sealed interface ProjectViewState {
    data object Initial : ProjectViewState
    data object ProjectFound : ProjectViewState // Doesn't really matter, because Main takes us to the app
    data object ProjectNotFound : ProjectViewState // Doesn't really matter, because Main takes us to the app
}