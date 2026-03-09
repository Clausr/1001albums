plugins {
    alias(libs.plugins.oag.android.library)
    alias(libs.plugins.oag.android.library.compose)
    alias(libs.plugins.oag.android.hilt)
}

android {
    namespace = "dk.clausr.core.common"

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.lifecycle.runtimeCompose)
}
