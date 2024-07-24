package dk.clausr.a1001albumsgenerator.onboarding

object OnboardingDirections {
    object Routes {
        internal const val ROOT = "onboarding"
        internal const val PROJECT_NAME = "$ROOT/project"
        internal const val STREAMING_PLATFORM = "$ROOT/streaming_platform"
    }

    fun projectName() = Routes.PROJECT_NAME
    fun streamingPlatform() = Routes.STREAMING_PLATFORM
}
