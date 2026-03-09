plugins {
    alias(libs.plugins.oag.jvm.library)
    id("kotlinx-serialization")
}

dependencies {
    implementation(libs.kotlinx.serialization.json)
}
