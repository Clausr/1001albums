plugins {
    alias(libs.plugins.oag.android.library)
    alias(libs.plugins.oag.android.hilt)
    id("kotlinx-serialization")
}

android {
    namespace = "dk.clausr.a1001albumsgenerator.datastore"
}

dependencies {
    implementation(project(":core:common"))
    implementation(libs.androidx.datastore.preferences)
}
