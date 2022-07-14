val projectsAlias = projects

val rootExamplesDir = projectDir
val rootExamplesBuildDir = buildDir

subprojects {
    group = "karth.plugins.examples"
    val relative = projectDir.relativeTo(rootExamplesDir)
    buildDir = rootExamplesBuildDir.resolve(relative)
    dependencies {
        implementation(projectsAlias.plugins.api)
    }
    sourceSets.main {
        java.srcDir("src")
        resources.srcDir("resources")
    }
}
