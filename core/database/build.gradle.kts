plugins {
    alias(libs.plugins.oag.android.library)
    alias(libs.plugins.oag.android.hilt)
    alias(libs.plugins.oag.android.room)
}

android {
    namespace = "dk.clausr.core.database"
}

dependencies {
    implementation(project(":core:model"))
    implementation(libs.kotlinx.coroutines.android)
}
