pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io") // Necessary for the gradle to be able to download github packages
    }
}

rootProject.name = "1001 Albums Generator"
include(":app")
include(":core:network")
include(":core:data")

include(":widget")
include(":core:common")
include(":core:model")
include(":feature:overview")
include(":core:database")
include(":core:data-widget")
include(":core:ui")
