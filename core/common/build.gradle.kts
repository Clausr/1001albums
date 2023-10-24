plugins {
    id("a1001albums.android.library")
    id("a1001albums.android.hilt")
}

android {
    namespace = "dk.clausr.core.common"
}

dependencies {
    implementation(libs.kotlinx.coroutines.android)
}
