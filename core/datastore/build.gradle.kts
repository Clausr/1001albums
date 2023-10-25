plugins {
    id("a1001albums.android.library")
    id("a1001albums.android.hilt")
}

android {
    namespace = "dk.clausr.core.datastore"
}

dependencies {
    implementation(libs.androidx.datastore.preferences)
}
