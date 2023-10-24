plugins {
    id("a1001albums.android.library")
}

android {
    namespace = "dk.clausr.core.data"

}

dependencies {
    implementation(project(":core:network"))
}
