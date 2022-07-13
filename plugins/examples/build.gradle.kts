val projectsAlias = projects

subprojects {
    group = "karth.plugins.examples"

    dependencies {
        implementation(projectsAlias.plugins.api)
    }

    sourceSets.main {
        java.srcDir("src")
        resources.srcDir("resources")
    }
}
