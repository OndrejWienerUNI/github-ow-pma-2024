pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    // Note: The 'RepositoriesMode.FAIL_ON_PROJECT_REPOS' feature is marked @Incubating.
    // This is intentional to enforce centralized repository configuration and prevent
    // project-level repositories. This aligns with best practices for dependency management.
    // Monitor future Gradle releases for any changes to the stability or behavior of this feature.
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}


rootProject.name = "template"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
include(":app")
include(":lint")
