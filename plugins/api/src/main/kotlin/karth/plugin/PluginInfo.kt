package karth.plugin

annotation class PluginInfo(
    val title: String,
    val author: String,
    val description: String = "",
    val version: String = "1.0",
    val fxmlFile: String,
    val iconFile: String,
    val resizeable: Boolean = false,
    val alwaysOnTop: Boolean = false,
) {

}
