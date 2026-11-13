pluginManagement {
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
        // 현대자동차그룹 Pleos Playground 공식 Maven 저장소
        maven {
            url = uri("https://nexus-playground.pleos.ai/repository/maven-releases/")
        }
    }
}

rootProject.name = "PleosWitness"
include(":app")
