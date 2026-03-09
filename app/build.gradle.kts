import dk.clausr.GitVersion
import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.oag.android.application)
    alias(libs.plugins.oag.android.application.compose)
    alias(libs.plugins.oag.android.application.flavors)
    alias(libs.plugins.oag.android.hilt)
    alias(libs.plugins.oag.android.room)
    alias(libs.plugins.oag.android.firebase)
}

val keystorePropertiesFile = rootProject.file("signing/secrets.properties")
val keystoreProperties = Properties()

val gitVersionName = GitVersion.resolveVersionName(project)
val gitVersionCode = GitVersion.resolveVersionCode(project)

android {
    namespace = "dk.clausr.a1001albumsgenerator"

    buildFeatures.buildConfig = true
    defaultConfig {
        applicationId = "dk.clausr.a1001albumsgenerator"

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
            applicationIdSuffix = ".debug"
        }
    }

    lint {
        checkReleaseBuilds = false
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:ui"))
    implementation(project(":core:network"))
    implementation(project(":core:data"))
    implementation(project(":core:model"))
    implementation(project(":core:analytics"))
    implementation(project(":widget"))
    implementation(project(":feature:overview"))
    implementation(project(":feature:onboarding"))

    implementation(libs.material)

    implementation(libs.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.activity.compose)

    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.animation.core)
    implementation(libs.androidx.compose.animation)

    implementation(libs.androidx.core.splashscreen)

    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.hilt.work)

    implementation(libs.coil3)
    implementation(libs.coil3.coil.compose)
    implementation(libs.coil3.coil.network.okhttp)
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
