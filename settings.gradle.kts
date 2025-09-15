pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "1001 Albums Generator"
include(":app")
include(":core:analytics")
include(":core:common")
include(":core:data")
include(":core:data-widget")
include(":core:database")
include(":core:datastore")
include(":core:model")
include(":core:network")
include(":core:ui")
include(":feature:onboarding")
include(":feature:overview")
include(":widget")
