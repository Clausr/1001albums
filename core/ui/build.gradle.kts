plugins {
    id("a1001albums.android.library")
    id("a1001albums.android.library.compose")
}

android {
    namespace = "dk.clausr.a1001albumsgenerator.ui"
}

dependencies {
    implementation(project(":core:model"))
    api(project(":core:analytics"))

    implementation(libs.material)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.animation)
    implementation(libs.androidx.compose.animation.core)

    implementation(libs.coil3)
    implementation(libs.coil3.coil.compose)
    implementation(libs.coil3.coil.network.okhttp)

    api(libs.haze)
    api(libs.haze.materials)

    api(libs.androidx.compose.material3)

    api(libs.androidx.compose.material.iconsExtended)
}
