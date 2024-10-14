import dk.clausr.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class AndroidWidgetConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply {
                apply("a1001albums.android.library")
            }

            dependencies {
                add("implementation", libs.findLibrary("kotlinx.coroutines.android").get())
                add("api", libs.findLibrary("androidx.glance.appwidget").get())
                add("implementation", libs.findLibrary("androidx.glance.material3").get())
                add("implementation", libs.findLibrary("androidx.glance.preview").get())
                add("implementation", libs.findLibrary("androidx.glance.appwidget.preview").get())

                add("implementation", libs.findLibrary("activity.compose").get())
                add("implementation", libs.findLibrary("androidx.compose.material3").get())
            }
        }
    }
}
