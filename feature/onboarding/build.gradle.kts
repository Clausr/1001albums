plugins {
    alias(libs.plugins.oag.android.feature)
    alias(libs.plugins.oag.android.library.compose)
}

android {
    namespace = "dk.clausr.a1001albumsgenerator.feature.onboarding"
}

dependencies {
    project(":widget")
}
