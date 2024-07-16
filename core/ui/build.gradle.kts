plugins {
    id("a1001albums.android.library")
    id("a1001albums.android.library.compose")
}

android {
    namespace = "dk.clausr.a1001albumsgenerator.ui"
}

dependencies {
    implementation(libs.material)
    api(libs.androidx.compose.material3)
    implementation(project(":core:model"))
}
