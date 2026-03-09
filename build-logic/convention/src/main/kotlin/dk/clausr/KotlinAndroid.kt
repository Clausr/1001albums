package dk.clausr

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.provideDelegate
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinBasePlugin
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

internal fun Project.configureKotlinAndroid(extension: ApplicationExtension) {
    configureKotlinAndroidCommon(extension)
    configureToolchains()
    configureKotlin()
    configureJava()
    configureKotlinAndroidDependencies()
}

internal fun Project.configureKotlinAndroid(extension: LibraryExtension) {
    configureKotlinAndroidCommon(extension)
    configureToolchains()
    configureKotlin()
    configureJava()
    configureKotlinAndroidDependencies()
}

/**
 * Configure base Kotlin with Android options
 */
internal fun Project.configureKotlinAndroidCommon(extension: ApplicationExtension) {
    extension.apply {
        compileSdk = Versions.COMPILE_SDK

        defaultConfig {
            minSdk = Versions.MIN_SDK
        }

        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_11
            targetCompatibility = JavaVersion.VERSION_11
            isCoreLibraryDesugaringEnabled = true
        }
    }
}

internal fun Project.configureKotlinAndroidCommon(extension: LibraryExtension) {
    extension.apply {
        compileSdk = Versions.COMPILE_SDK

        defaultConfig {
            minSdk = Versions.MIN_SDK
        }

        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_11
            targetCompatibility = JavaVersion.VERSION_11
            isCoreLibraryDesugaringEnabled = true
        }
    }
}

private fun Project.configureKotlinAndroidDependencies() {
    dependencies {
        add("coreLibraryDesugaring", libs.findLibrary("android.desugarJdkLibs").get())
        add("implementation", libs.findLibrary("timber").get())
    }
}

fun Project.configureJava() {
    val jvmTarget = Versions.getJvmTarget(this).toInt()
    plugins.withType(JavaBasePlugin::class.java).configureEach {
        project.configure<JavaPluginExtension> {
            val version = JavaVersion.toVersion(jvmTarget)
            sourceCompatibility = version
            targetCompatibility = version
        }
    }
}

/**
 * Configure base Kotlin options for JVM (non-Android)
 */
internal fun Project.configureKotlinJvm() {
    extensions.configure<JavaPluginExtension> {
        // Up to Java 11 APIs are available through desugaring
        // https://developer.android.com/studio/write/java11-minimal-support-table
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    configureKotlin()
}

/**
 * Configure base Kotlin options
 */
private fun Project.configureKotlin() {
    // Use withType to workaround https://youtrack.jetbrains.com/issue/KT-55947
    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions {
            // Set JVM target to 11
            jvmTarget.set(JvmTarget.JVM_11)

            // Treat all Kotlin warnings as errors (disabled by default)
            // Override by setting warningsAsErrors=true in your ~/.gradle/gradle.properties
            val warningsAsErrors: String? by project
            allWarningsAsErrors.set(warningsAsErrors.toBoolean())
            freeCompilerArgs.addAll(
                listOf(
                    // Enable experimental coroutines APIs
                    "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
                    "-XXLanguage:+PropertyParamAnnotationDefaultTargetMode",
                ),
            )
        }
    }
}

private fun Project.configureToolchains() {
    val jdkVersion = Versions.getJdkVersion(this@configureToolchains)
    val javaLanguageVersion = JavaLanguageVersion.of(jdkVersion)

    plugins.withType(JavaBasePlugin::class.java) {
        extensions.configure<JavaPluginExtension> {
            toolchain {
                languageVersion.set(javaLanguageVersion)
            }
        }
    }
    plugins.withType(KotlinBasePlugin::class.java) {
        kotlinExtension.jvmToolchain {
            languageVersion.set(javaLanguageVersion)
        }
    }
}