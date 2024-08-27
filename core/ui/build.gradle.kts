plugins {
    id("a1001albums.android.library")
    id("a1001albums.android.library.compose")
}

android {
    namespace = "dk.clausr.a1001albumsgenerator.ui"
}

dependencies {
    implementation(project(":core:model"))

    implementation(libs.material)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.compose.animation)
    implementation(libs.compose.animation.core)

    implementation(libs.coil3)
    implementation(libs.coil3.coil.compose)
    implementation(libs.coil3.coil.network.okhttp)

    implementation(libs.haze)
    api(libs.androidx.compose.material3)
}
