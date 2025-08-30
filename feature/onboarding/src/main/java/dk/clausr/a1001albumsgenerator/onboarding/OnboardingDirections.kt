package dk.clausr.a1001albumsgenerator.onboarding

import androidx.navigation.NavController
import kotlinx.serialization.Serializable

@Serializable
object ProjectNameRoute

@Serializable
object StreamingPlatformRoute

internal fun NavController.navigateToStreamingPlatform() = navigate(StreamingPlatformRoute)