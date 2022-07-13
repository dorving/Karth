val projectsAlias = projects
val libsAlias = libs

plugins {
    id("org.jetbrains.kotlin.plugin.serialization")
}

dependencies {
    implementation(projectsAlias.protocol.habbo)
    implementation(libsAlias.tornadoFX)
    implementation(libsAlias.kotlinSerialisationJson)
}