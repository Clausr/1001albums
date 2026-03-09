package dk.clausr

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

/**
 * Configure Compose-specific options
 */
internal fun Project.configureAndroidCompose(extension: ApplicationExtension) {
    configureComposeBuildFeatures(extension)
    configureComposeDependencies()
    configureComposeCompiler()
}

/**
 * Configure Compose-specific options for LibraryExtension
 */
internal fun Project.configureAndroidCompose(extension: LibraryExtension) {
    configureComposeBuildFeatures(extension)
    configureComposeDependencies()
    configureComposeCompiler()
}

private fun Project.configureComposeCompiler() {
    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions {
            freeCompilerArgs.addAll(
                listOf(
                    "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
                    "-opt-in=androidx.compose.animation.ExperimentalSharedTransitionApi",
                ),
            )
        }
    }
}

private fun configureComposeBuildFeatures(extension: ApplicationExtension) {
    extension.apply {
        buildFeatures {
            compose = true
        }
    }
}

private fun configureComposeBuildFeatures(extension: LibraryExtension) {
    extension.apply {
        buildFeatures {
            compose = true
        }
    }
}

private fun Project.configureComposeDependencies() {
    dependencies {
        val bom = libs.findLibrary("androidx-compose-bom").get()
        add("implementation", platform(bom))
        add("androidTestImplementation", platform(bom))
        add("implementation", libs.findLibrary("androidx.compose.ui.tooling.preview").get())
        add("debugImplementation", libs.findLibrary("androidx.compose.ui.tooling").get())
        add("implementation", libs.findLibrary("kotlinx.collections.immutable").get())
    }
}
