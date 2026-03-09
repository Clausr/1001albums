plugins {
    alias(libs.plugins.oag.android.feature)
    alias(libs.plugins.oag.android.library.compose)
}

android {
    namespace = "dk.clausr.feature.overview"
}

dependencies {
    implementation(project(":core:data-widget"))
    implementation(project(":widget"))

    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.coil3)
    implementation(libs.coil3.coil.compose)
    implementation(libs.coil3.coil.network.okhttp)
    implementation(libs.kotlinx.collections.immutable)
}
