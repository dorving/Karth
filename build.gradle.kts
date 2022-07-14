
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val libsAlias = libs
val projectsAlias = projects

plugins {
    kotlin("jvm")
    id("org.openjfx.javafxplugin") apply false
}

allprojects {
    group = "karth"
    version = "0.0.1"

    apply {
        plugin("org.jetbrains.kotlin.jvm")
    }

    repositories {
        mavenCentral()
        maven("https://jitpack.io")
        maven("https://dl.bintray.com/michaelbull/maven")
        maven("https://maven.pkg.jetbrains.space/data2viz/p/maven/dev")
        maven("https://maven.pkg.jetbrains.space/data2viz/p/maven/public")
    }

    dependencies {
        implementation(kotlin("stdlib"))
        implementation(libsAlias.slf4j)
        implementation(libsAlias.inlineLogger)
    }

    tasks.withType<JavaCompile> {
        options.release.set(17)
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "17"
        }
    }
}