val projectsAlias = projects
val libsAlias = libs

plugins {
    `maven-publish`
}

dependencies {
    implementation(projectsAlias.core)
    implementation(libs.tornadoFX)
    implementation("io.netty:netty-all:4.1.24.Final")
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