plugins {
    id("a1001albums.jvm.library")
    id("kotlinx-serialization")
}

dependencies {
    implementation(libs.kotlinx.serialization.json)
}
