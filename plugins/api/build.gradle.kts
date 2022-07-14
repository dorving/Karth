val projectsAlias = projects
val libsAlias = libs

plugins {
    `maven-publish`
}

dependencies {
    api(projectsAlias.core)
    implementation(libsAlias.tornadoFX)
    implementation(libsAlias.netty)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "karth.plugins"
            artifactId = "api"
            from(components["java"])
        }
    }
}