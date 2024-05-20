plugins {
    id("a1001albums.android.library")
    id("a1001albums.android.hilt")
}

android {
    namespace = "dk.clausr.core.data"

}

dependencies {
    implementation(project(":core:network"))
    implementation(project(":core:common"))
    implementation(project(":core:model"))
    implementation(project(":core:database"))
    api(project(":core:data-widget"))

    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.datastore.preferences)

    api(libs.androidx.work.ktx)
    api(libs.androidx.hilt.work)
    implementation(libs.hilt.ext.work)

    kapt(libs.hilt.ext.compiler)

}
