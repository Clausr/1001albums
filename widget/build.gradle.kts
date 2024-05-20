plugins {
    id("a1001albums.android.library.compose")
    id("a1001albums.android.widget")
    id("a1001albums.android.hilt")
    id("kotlinx-serialization")
}

android {
    namespace = "dk.clausr.widget"
}

dependencies {
    implementation(project(":core:data"))
    implementation(project(":core:common"))
    implementation(project(":core:model"))


    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.compose.material.iconsExtended)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.work.ktx)
    implementation(libs.androidx.hilt.work)
    implementation(libs.hilt.ext.work)
    kapt(libs.hilt.ext.compiler)
}
