plugins {
    id("a1001albums.android.library")
    id("a1001albums.android.library.compose")
    id("a1001albums.android.hilt")
}

android {
    namespace = "dk.clausr.a1001albumsgenerator.core.analytics"

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    prodImplementation(platform(libs.firebase.bom))
    prodImplementation(libs.firebase.analytics)
}