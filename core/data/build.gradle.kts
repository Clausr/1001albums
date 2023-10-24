plugins {
    id("a1001albums.android.library")
    id("a1001albums.android.hilt")

}

android {
    namespace = "dk.clausr.core.data"

}

dependencies {
    implementation(project(":core:network"))
    implementation(project(":core:common"))
    implementation(project(":core:model"))
    implementation(libs.kotlinx.coroutines.android)

}
