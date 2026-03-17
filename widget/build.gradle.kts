plugins {
    id("org.jetbrains.kotlin.plugin.compose")
    alias(libs.plugins.oag.android.widget)
    alias(libs.plugins.oag.android.hilt)
    id("com.google.devtools.ksp")
}

android {
    namespace = "dk.clausr.widget"

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":core:data"))
    implementation(project(":core:data-widget"))
    implementation(project(":core:common"))
    implementation(project(":core:model"))
    implementation(project(":core:ui"))
    implementation(project(":feature:onboarding"))
    implementation("com.google.guava:guava:33.5.0-android")
    implementation(libs.kotlinx.collections.immutable)

    implementation(libs.androidx.glance.preview)

    implementation(libs.material)

    implementation(libs.coil3)
    implementation(libs.coil3.coil.compose)
    implementation(libs.coil3.coil.network.okhttp)

    implementation(libs.androidx.hilt.navigation.compose)

    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.work.ktx)
    implementation(libs.androidx.hilt.work)

    ksp(libs.hilt.ext.compiler)
}
