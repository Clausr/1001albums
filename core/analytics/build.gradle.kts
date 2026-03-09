plugins {
    alias(libs.plugins.oag.android.library)
    alias(libs.plugins.oag.android.library.compose)
    alias(libs.plugins.oag.android.hilt)
}

android {
    namespace = "dk.clausr.a1001albumsgenerator.core.analytics"

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
}
