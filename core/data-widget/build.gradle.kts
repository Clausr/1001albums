plugins {
    id("a1001albums.android.library")
    id("a1001albums.android.hilt")
    id("a1001albums.android.widget")
    id("kotlinx-serialization")
}

android {
    namespace = "dk.clausr.core.data_widget"

}

dependencies {
    implementation(libs.kotlinx.serialization.json)
    implementation(project(":core:model"))
}
