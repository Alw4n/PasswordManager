import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.gradle.jvm.tasks.Jar

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    kotlin("plugin.serialization") version "2.2.0"
}

kotlin {
    jvm()

    sourceSets {
        commonMain {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                implementation(compose.components.resources)
                implementation(compose.components.uiToolingPreview)
                implementation(libs.androidx.lifecycle.viewmodelCompose)
                implementation(libs.androidx.lifecycle.runtimeCompose)

                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
                runtimeOnly("org.slf4j:slf4j-nop:2.0.7")
            }
        }
        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
        jvmMain {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(libs.kotlinx.coroutinesSwing)
                implementation("org.xerial:sqlite-jdbc:3.41.2.1")
                implementation("org.jetbrains.exposed:exposed-core:0.41.1")
                implementation("org.jetbrains.exposed:exposed-dao:0.41.1")
                implementation("org.jetbrains.exposed:exposed-jdbc:0.41.1")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
                implementation("app.cash.sqldelight:coroutines-extensions-jvm:2.1.0")
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "org.example.project.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb, TargetFormat.Exe)
            packageName = "MyPasswordManager"
            packageVersion = "1.0.0"
            description = "Password manager"
            vendor = "Your Company"

            windows {
                // iconFile.set(project.file("src/jvmMain/resources/app_icon.ico"))
            }
        }
    }
}

/*
  Fat JAR task for JVM target.
  - archiveBaseName: имя итогового jar
  - manifest Main-Class: укажите ваш main класс
  - duplicatesStrategy: исключаем дубли
*/
tasks.register<Jar>("fatJar") {
    dependsOn("jvmJar")
    archiveBaseName.set("MyPasswordManager-all")
    archiveVersion.set("") // если не хотите версию в имени
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    manifest {
        attributes["Main-Class"] = "org.example.project.MainKt"
    }

    // Включаем скомпилированный код JVM
    from(kotlin.targets.getByName("jvm").compilations.getByName("main").output)

    // Берём runtime classpath для JVM и распаковываем зависимости внутрь jar
    val runtimeClasspath = configurations.getByName("jvmRuntimeClasspath")
    from(runtimeClasspath.map { if (it.isDirectory) it else zipTree(it) })
}
