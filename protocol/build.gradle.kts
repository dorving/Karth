val libsAlias = libs
val projectsAlias = projects

subprojects {
    group = "karth.protocol"
    dependencies {
        implementation(projectsAlias.core)
    }
}
