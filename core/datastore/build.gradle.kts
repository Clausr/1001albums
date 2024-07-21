plugins {
    id("a1001albums.android.library")
    id("a1001albums.android.hilt")
    id("kotlinx-serialization")
}

android {
    namespace = "dk.clausr.a1001albumsgenerator.datastore"
}

dependencies {
    implementation(project(":core:common"))
    implementation(libs.androidx.datastore.preferences)
}