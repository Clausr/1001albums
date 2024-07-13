import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

group = "dk.clausr.buildLogic"

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.ksp.gradlePlugin)

}

gradlePlugin {
    plugins {
        register("androidApplicationCompose") {
            id = "a1001albums.android.application.compose"
            implementationClass = "AndroidApplicationComposeConventionPlugin"
        }
        register("androidApplication") {
            id = "a1001albums.android.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("androidLibraryCompose") {
            id = "a1001albums.android.library.compose"
            implementationClass = "AndroidLibraryComposeConventionPlugin"
        }
        register("androidLibrary") {
            id = "a1001albums.android.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("androidFeature") {
            id = "a1001albums.android.feature"
            implementationClass = "AndroidFeatureConventionPlugin"
        }
        register("androidHilt") {
            id = "a1001albums.android.hilt"
            implementationClass = "AndroidHiltConventionPlugin"
        }
        register("androidFlavors") {
            id = "a1001albums.android.application.flavors"
            implementationClass = "AndroidApplicationFlavorsConventionPlugin"
        }
        register("jvmLibrary") {
            id = "a1001albums.jvm.library"
            implementationClass = "JvmLibraryConventionPlugin"
        }
        register("androidWidget") {
            id = "a1001albums.android.widget"
            implementationClass = "AndroidWidgetConventionPlugin"
        }
        register("androidRoom") {
            id = "a1001albums.android.room"
            implementationClass = "AndroidRoomConventionPlugin"
        }
    }
}
