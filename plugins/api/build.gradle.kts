val projectsAlias = projects
val libsAlias = libs

dependencies {
    implementation(projectsAlias.core)
    implementation(libs.tornadoFX)
    implementation("io.netty:netty-all:4.1.24.Final")
}
