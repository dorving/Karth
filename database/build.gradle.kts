val projectsAlias = projects

dependencies {
    implementation(projectsAlias.core)
    implementation("org.jetbrains.exposed", "exposed-core", "0.38.1")
    implementation("org.jetbrains.exposed", "exposed-dao", "0.38.1")
    implementation("org.jetbrains.exposed", "exposed-jdbc", "0.38.1")
    implementation("com.h2database","h2", "2.1.214")
}