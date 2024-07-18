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
    api(libs.androidx.compose.material3)
}
