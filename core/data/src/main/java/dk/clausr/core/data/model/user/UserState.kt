package dk.clausr.core.data.model.user

import dk.clausr.core.model.StreamingPlatform

sealed interface UserState {
    data object NotOnboarded : UserState
    data class Onboarding(val projectId: String?, val preferredStreamingPlatform: StreamingPlatform?) : UserState
    data class Active(val projectId: String, val preferredStreamingPlatform: StreamingPlatform?) : UserState
}