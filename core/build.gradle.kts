val libsAlias = libs

plugins {
    kotlin("plugin.serialization")
    `maven-publish`
}

dependencies {
    api(libsAlias.gearth)
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