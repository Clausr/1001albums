package dk.clausr

import org.gradle.api.Project
import org.gradle.api.artifacts.VersionConstraint
import kotlin.jvm.optionals.getOrNull

object Versions {
    const val TARGET_VERSION = 36
    const val COMPILE_SDK = 36
    const val MIN_SDK = 26

    fun getJdkVersion(project: Project): String {
        return checkNotNull(project.libs.findVersion("jdk").map(VersionConstraint::toString).getOrNull()) {
            "No JDK version found in the version catalog with the key 'jdk'"
        }
    }

    fun getJvmTarget(project: Project): String {
        return checkNotNull(project.libs.findVersion("jvmTarget").map(VersionConstraint::toString).getOrNull()) {
            "No JVM target found in the version catalog with the key 'jvmTarget'"
        }
    }
}
