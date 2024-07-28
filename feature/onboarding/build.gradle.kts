plugins {
    id("a1001albums.android.feature")
    id("a1001albums.android.library.compose")
}

android {
    namespace = "dk.clausr.a1001albumsgenerator.feature.onboarding"

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.haze)
    implementation(libs.androidx.compose.material.iconsExtended)
}
