plugins {
    id("a1001albums.android.library")
    id("a1001albums.android.hilt")
    id("kotlinx-serialization")
}

android {
    namespace = "dk.clausr.a1001albumsgenerator.network"

    buildFeatures {
        buildConfig = true
    }
    defaultConfig {

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

        buildConfigField("String", "BACKEND_URL", "\"https://1001albumsgenerator.com/\"")
    }
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:model"))

    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.retrofit.kotlin.serialization)
    implementation(libs.retrofit.core)

    implementation(libs.okhttp.logging)
    implementation(libs.coil.kt)
}
