rootProject.name = "KotlinProject"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        // Gradle Plugin Portal (основной реестр плагинов)
        gradlePluginPortal()

        // дополнительные репозитории на случай, если плагин там размещён
        mavenCentral()
        google()

        // явный maven репозиторий для плагинов (иногда требуется)
        maven("https://plugins.gradle.org/m2/")

        // если используешь JetBrains Compose dev репозиторий для плагинов/артефактов
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        // Compose dev repo (на случай, если у тебя dev-артефакты)
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

include(":composeApp")
