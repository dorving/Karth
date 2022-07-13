val libsAlias = libs
val projectsAlias = projects

val rootPluginDir = projectDir
val rootPluginBuildDir = buildDir

subprojects {
    val relative = projectDir.relativeTo(rootPluginDir)
    buildDir = rootPluginBuildDir.resolve(relative)
    group = "karth.protocol"
    dependencies {
        implementation(projectsAlias.core)
    }
}
