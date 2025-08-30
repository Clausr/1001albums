package dk.clausr.a1001albumsgenerator.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dk.clausr.core.common.model.doOnFailure
import dk.clausr.core.common.model.doOnSuccess
import dk.clausr.core.common.network.AppInformation
import dk.clausr.core.data.repository.NotificationRepository
import dk.clausr.core.data.repository.OagRepository
import dk.clausr.core.data.repository.UserRepository
import dk.clausr.core.model.StreamingPlatform
import dk.clausr.core.network.NetworkError
import dk.clausr.core.ui.CoverData
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val oagRepository: OagRepository,
    private val userRepository: UserRepository,
    private val notificationRepository: NotificationRepository,
    appInformation: AppInformation,
) : ViewModel() {
    private val buildVersion = "${appInformation.versionName} (${appInformation.versionCode})"
    private var _error = MutableStateFlow<NetworkError?>(null)

    val viewState: StateFlow<ViewState> =
        combine(
            oagRepository.projectId,
            oagRepository.preferredStreamingPlatform,
            oagRepository.albumCovers,
            _error.asStateFlow(),
        ) { projectId, streamingPlatform, albumCovers, error ->
            ViewState(
                projectId = projectId,
                editProjectIdEnabled = projectId == null,
                preferredStreamingPlatform = streamingPlatform,
                covers = albumCovers,
                error = error,
                buildVersion = buildVersion,
            )
        }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = ViewState(),
            )

    fun setProjectId(projectId: String) {
        Timber.d("setProjectId $projectId")
        val encodedProjectId = projectId.replace(" ", "-")

        viewModelScope.launch {
            val existingProject = oagRepository.project.firstOrNull()
            if (existingProject != null && existingProject.name.equals(encodedProjectId, ignoreCase = true)) {
                Timber.d("Same as existing project: ${existingProject.name}")
            } else {
                val asyncSetProject = async { oagRepository.setProject(encodedProjectId) }
                val asyncUpdateNotifications = async {
                    notificationRepository.updateNotifications(
                        origin = "SettingsVM",
                        projectId = encodedProjectId,
                        getRead = false,
                    )
                }

                asyncSetProject.await()
                    .doOnSuccess {
                        Timber.d("Set project success!")
                        _error.update { null }
                        asyncUpdateNotifications.await()
                    }
                    .doOnFailure { error ->
                        Timber.e(error.cause, "Could not change projectId")
                        _error.update { error }
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

    data class ViewState(
        val projectId: String? = null,
        val editProjectIdEnabled: Boolean = true,
        val preferredStreamingPlatform: StreamingPlatform = StreamingPlatform.Undefined,
        val covers: CoverData = CoverData.default(),
        val error: NetworkError? = null,
        val buildVersion: String = "",
    )
}
