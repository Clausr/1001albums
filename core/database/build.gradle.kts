plugins {
    id("a1001albums.android.library")
    id("a1001albums.android.hilt")
    id("a1001albums.android.room")
}

android {
    namespace = "dk.clausr.core.database"
}

dependencies {
    implementation(project(":core:model"))
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.google.gson)
}
