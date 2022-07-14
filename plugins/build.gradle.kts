val projectsAlias = projects
val libsAlias = libs

plugins {
    id("org.openjfx.javafxplugin")
}

subprojects {
    group = "karth.plugins"
    apply(plugin = "org.openjfx.javafxplugin")
    dependencies {
        implementation(projectsAlias.core)
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
}
