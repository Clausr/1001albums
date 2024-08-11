plugins {
    id("a1001albums.android.feature")
    id("a1001albums.android.library.compose")
}

android {
    namespace = "dk.clausr.feature.overview"
}

dependencies {
    implementation(project(":core:data-widget"))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.coil.kt)
    implementation(libs.coil.kt.compose)
    implementation(libs.kotlinx.collections.immutable)
}
