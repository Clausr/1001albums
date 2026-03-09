plugins {
    alias(libs.plugins.oag.android.library)
    alias(libs.plugins.oag.android.hilt)
    alias(libs.plugins.oag.android.widget)
    id("kotlinx-serialization")
}

android {
    namespace = "dk.clausr.core.data_widget"
}

dependencies {
    implementation(libs.kotlinx.serialization.json)
    implementation(project(":core:model"))
    implementation(project(":core:common"))
}
