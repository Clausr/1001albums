plugins {
    id("a1001albums.android.library")
    id("a1001albums.android.hilt")
    id("kotlinx-serialization")
}

android {
    namespace = "dk.clausr.core.data"
}

dependencies {
    implementation(project(":core:network"))
    implementation(project(":core:common"))
    implementation(project(":core:model"))
    implementation(project(":core:database"))
    implementation(project(":core:data-widget"))
    implementation(project(":core:datastore"))

    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.datastore.preferences)

    api(libs.androidx.work.ktx)
    api(libs.androidx.hilt.work)

    implementation(libs.kotlinx.serialization.json)

    ksp(libs.hilt.ext.compiler)
}
