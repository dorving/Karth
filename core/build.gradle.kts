val libsAlias = libs

plugins {
    id("org.jetbrains.kotlin.plugin.serialization")
}

dependencies {
    api("com.github.dorving:G-Earth:master-SNAPSHOT")
    implementation(libsAlias.kotlinCoroutinesCore)
    implementation(libsAlias.kotlinSerialisationJson)
    implementation(libsAlias.kotlinReflect)
}
