import java.io.FileInputStream
import java.util.Properties

plugins {
    id("a1001albums.android.application")
    id("a1001albums.android.application.compose")
    id("a1001albums.android.application.flavors")
    id("a1001albums.android.hilt")
}

val keystorePropertiesFile = rootProject.file("signing/secrets.properties")
val keystoreProperties = Properties()
keystoreProperties.load(FileInputStream(keystorePropertiesFile))

fun getGitCommitCount(): Int {
    val process = Runtime.getRuntime().exec("git rev-list --count HEAD")
    return process.inputStream.bufferedReader().use { it.readText().trim().toInt() }
}

android {
    namespace = "dk.clausr.a1001albumsgenerator"

    defaultConfig {
        applicationId = "dk.clausr.a1001albumsgenerator"
        versionCode = getGitCommitCount()
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        create("release") {
            storeFile = rootProject.file("signing/Clausr.keystore")
            storePassword = keystoreProperties["SIGNING_STORE_PASSWORD"] as String
            keyAlias = keystoreProperties["GOOGLE_PLAY_SIGNING_KEY_ALIAS"] as String
            keyPassword = keystoreProperties["SIGNING_KEY_PASSWORD"] as String
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
        debug {
            isMinifyEnabled = false
        }
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(project(":core:network"))
    implementation(project(":core:data"))
    implementation(project(":core:model"))
    implementation(project(":widget"))
    implementation(project(":feature:overview"))

    implementation(libs.material)

    implementation(libs.core.ktx)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.activity.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.hilt.work)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
