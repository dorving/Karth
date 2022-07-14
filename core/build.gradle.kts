val libsAlias = libs

plugins {
    id("org.jetbrains.kotlin.plugin.serialization")
    `maven-publish`
}

dependencies {
    api("com.github.dorving:G-Earth:master-SNAPSHOT")
    implementation(libsAlias.kotlinCoroutinesCore)
    implementation(libsAlias.kotlinSerialisationJson)
    implementation(libsAlias.kotlinReflect)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "karth"
            artifactId = "core"
            from(components["java"])
        }
    }
}