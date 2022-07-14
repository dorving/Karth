val libsAlias = libs
val projectsAlias = projects

plugins {
    `maven-publish`
}

subprojects {
    group = "karth.protocol"
    dependencies {
        implementation(projectsAlias.core)
    }
    apply {
        plugin("org.gradle.maven-publish")
    }

    publishing {
        publications {
            create<MavenPublication>("maven") {
                groupId = "karth.protocols"
                artifactId = this@subprojects.name
                from(components["java"])
            }
        }
    }
}
