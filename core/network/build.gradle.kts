plugins {
    id("a1001albums.android.library")
    id("kotlinx-serialization")

}

android {
    namespace = "dk.clausr.a1001albumsgenerator.network"

    defaultConfig {

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

}

dependencies {
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)
}
