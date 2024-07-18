plugins {
    id("a1001albums.android.library")
    id("a1001albums.android.library.compose")
    id("a1001albums.android.hilt")
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
