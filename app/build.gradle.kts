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
//keystoreProperties.load(FileInputStream(keystorePropertiesFile))

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
//        val keystoreProperties = Properties()
//        val keystorePropertiesFile = file("../signing/secrets.properties")
        if (keystorePropertiesFile.exists()) {
            keystoreProperties.load(FileInputStream(keystorePropertiesFile))
        }

        create("release") {
            storeFile = rootProject.file("signing/Clausr.keystore")
            storePassword = getPropertyOrEnvNullable("SIGNING_STORE_PASSWORD", keystoreProperties)
            keyAlias = getPropertyOrEnvNullable("GOOGLE_PLAY_SIGNING_KEY_ALIAS", keystoreProperties)
            keyPassword = getPropertyOrEnvNullable("SIGNING_KEY_PASSWORD", keystoreProperties)
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

    implementation(libs.androidx.core.splashscreen)

    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.hilt.work)
    debugImplementation(libs.androidx.compose.ui.tooling)
}

private fun getEnvNullable(variableName: String): String {
    return System.getenv(variableName).takeIf { it.isNotEmpty() } ?: "\"\""
}

fun getPropertyOrEnvNullable(variableName: String, keystoreProperties: Properties): String {
    val variable = keystoreProperties[variableName] as String?
    val output = variable ?: getEnvNullable(variableName)

    return output
}
