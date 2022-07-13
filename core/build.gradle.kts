val libsAlias = libs

plugins {
    id("org.jetbrains.kotlin.plugin.serialization")
}

dependencies {
    api(files("libs/G-Earth.jar"))
    implementation(libsAlias.kotlinCoroutinesCore)
    implementation(libsAlias.kotlinSerialisationJson)
}
