// Top-level build file where you can add configuration options common to all sub-projects/modules.

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.org.jetbrains.kotlin.android) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.detekt).version(libs.versions.detektPluginVersion.get())
    alias(libs.plugins.detekt.compiler) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
}

detekt {
    parallel = true
    buildUponDefaultConfig = true
    source.setFrom(projectDir)
    config.setFrom("$rootDir/config/detekt/detekt.yml")
    baseline = file("$projectDir/config/detekt/detekt-baseline.xml")
    enableCompilerPlugin = true
    debug = false
}

configurations {
    detektPlugins
}

dependencies {
    detektPlugins(libs.detekt.formatting)
    detektPlugins(libs.detekt.cli)
    detektPlugins(libs.detekt.compose.rules) {
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-compiler-embeddable")
    }
}
