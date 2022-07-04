plugins {
    java
    kotlin("jvm") version "1.6.10"
    kotlin("plugin.serialization") version "1.6.10"
    id("org.openjfx.javafxplugin") version "0.0.11"
}

group = "karth"
version = "0.1"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/data2viz/p/maven/dev")
    maven("https://maven.pkg.jetbrains.space/data2viz/p/maven/public")
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(files("lib/G-Earth.jar"))
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.7.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-javafx:1.6.2")
    implementation("no.tornado:tornadofx:1.7.20")

    testImplementation(kotlin("test"))
    testImplementation("io.mockk:mockk:1.12.4")
    testImplementation("org.jetbrains.kotlin:kotlin-reflect:1.7.0")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    sourceCompatibility = JavaVersion.VERSION_17
}

javafx {
    version = "17.0.2"
    modules(
        "javafx.base",
        "javafx.controls",
        "javafx.fxml",
        "javafx.graphics",
        "javafx.media",
        "javafx.swing",
        "javafx.web"
    )
}

tasks.test {
    useJUnitPlatform()
}
