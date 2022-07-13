import java.nio.file.Path

enableFeaturePreview("VERSION_CATALOGS")
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "karth"

include("core")
include("database")
include("launcher")
include("plugins")
includeChildren("plugins")
include("protocol")
includeChildren("protocol")

pluginManagement {
    plugins {
        kotlin("jvm") version "1.7.10"
        kotlin("plugin.serialization") version "1.7.10"
        id("org.jmailen.kotlinter") version "3.3.0"
        id("org.openjfx.javafxplugin") version "0.0.11"
    }
}


fun includeChildren(type: String) {
    val pluginPath: Path = project(":$type").projectDir.toPath()
    java.nio.file.Files.walk(pluginPath).forEach {
        if (!java.nio.file.Files.isDirectory(it)) {
            return@forEach
        }
        search(pluginPath, it, type)
    }
}

fun search(parent: Path, path: Path, type: String) {
    val hasBuildFile = java.nio.file.Files.exists(path.resolve("build.gradle.kts"))
    if (!hasBuildFile) {
        return
    }
    val relativePath = parent.relativize(path)
    val pluginName = relativePath.toString().replace(File.separator, ":")

    include("$type:$pluginName")
}
