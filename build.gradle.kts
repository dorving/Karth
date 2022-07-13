import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jmailen.gradle.kotlinter.KotlinterExtension
import org.jmailen.gradle.kotlinter.KotlinterPlugin

val libsAlias = libs
val projectsAlias = projects

plugins {
    kotlin("jvm")
    id("org.jmailen.kotlinter") apply false
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
            freeCompilerArgs = listOf("-XXLanguage:+InlineClasses")
        }
    }

    plugins.withType<KotlinterPlugin> {
        configure<KotlinterExtension> {
            disabledRules = arrayOf(
                "filename",
                /* https://github.com/pinterest/ktlint/issues/764 */
                "parameter-list-wrapping",
                /* https://github.com/pinterest/ktlint/issues/527 */
                "import-ordering"
            )
        }
    }

    plugins.withType<KotlinPluginWrapper> {
        apply(plugin = "org.jmailen.kotlinter")
    }
}
