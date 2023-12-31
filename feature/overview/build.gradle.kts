plugins {
    id("a1001albums.android.feature")
    id("a1001albums.android.library.compose")

}

android {
    namespace = "dk.clausr.feature.overview"

}

dependencies {
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.coil.kt)
    implementation(libs.coil.kt.compose)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
