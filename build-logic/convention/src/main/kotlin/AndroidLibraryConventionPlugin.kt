import com.android.build.gradle.LibraryExtension
import dk.clausr.Versions
import dk.clausr.configureFlavors
import dk.clausr.configureKotlinAndroid
import dk.clausr.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.kotlin

class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.library")
                apply("org.jetbrains.kotlin.android")
            }

            extensions.configure<LibraryExtension> {
                configureKotlinAndroid(this)
                defaultConfig.targetSdk = Versions.TargetVersion
                configureFlavors(this)
            }

            configurations.configureEach {
                resolutionStrategy {
                    // No clue what causes this to be some other version...
                    force(libs.findLibrary("androidx-navigation-compose").get())
                }
            }
            dependencies {
                add("androidTestImplementation", kotlin("test"))
                add("testImplementation", kotlin("test"))
            }
        }
    }
}
