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

apply("${project.rootDir}/gradle/script-git-version.gradle.kts")

android {
    namespace = "dk.clausr.a1001albumsgenerator"

    defaultConfig {
        applicationId = "dk.clausr.a1001albumsgenerator"

        val gitVersionName: String by extra
        val gitVersionCode: Int by extra
        versionName = gitVersionName
        versionCode = gitVersionCode

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
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
                "proguard-rules.pro",
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
    implementation(project(":core:ui"))
    implementation(project(":core:network"))
    implementation(project(":core:data"))
    implementation(project(":core:model"))
    implementation(project(":widget"))
    implementation(project(":feature:overview"))
    implementation(project(":feature:onboarding"))

    implementation(libs.material)

    implementation(libs.core.ktx)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.activity.compose)

    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.compose.animation.core)
    implementation(libs.compose.animation)

    implementation(libs.androidx.core.splashscreen)

    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.hilt.work)

    implementation(libs.coil.kt)
}

private fun getEnvNullable(variableName: String): String {
    return System.getenv(variableName)?.takeIf { it.isNotEmpty() } ?: "\"\""
}

fun getPropertyOrEnvNullable(
    variableName: String,
    keystoreProperties: Properties,
): String {
    val variable = keystoreProperties[variableName] as String?
    val output = variable ?: getEnvNullable(variableName)

    return output
}

sentry {
    org.set("clausr")
    projectName.set("1001-albums-widget")

    // this will upload your source code to Sentry to show it as part of the stack traces
    // disable if you don't want to expose your sources
    includeSourceContext.set(true)
}
