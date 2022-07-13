package gearth.extensions

import gearth.misc.listenerpattern.Observable
import javafx.application.Application
import javafx.application.Platform
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.image.Image
import javafx.stage.Stage
import karth.plugin.ExtensionClient
import karth.plugin.Plugin
import karth.plugin.PluginInfo
import kotlin.concurrent.thread
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.findAnnotation

class PluginApplication : Application() {

    companion object {
        lateinit var args: Array<String>
        lateinit var info: PluginInfo
        lateinit var creator: ExtensionFormCreator
    }

    override fun start(primaryStage: Stage) {
        val plugin = creator.createForm(primaryStage) as Plugin<*>
        val wrapper = object : Extension(args) {
            override fun initExtension() = plugin.initExtension()
            override fun onClick() = plugin.onClick()
            override fun onStartConnection() = plugin.onStartConnection()
            override fun onEndConnection() = plugin.onEndConnection()
            override fun getInfoAnnotations() = ExtensionInfo(
                Title = info.title,
                Description = info.description,
                Version = info.version,
                Author = info.author
            )
            override fun canLeave(): Boolean = plugin.canLeave()
            override fun canDelete(): Boolean = plugin.canDelete()
        }
        val hostServicesField = ExtensionForm::class.java.getDeclaredField("hostServices")
        hostServicesField.set(plugin, hostServices)
        val extensionField = ExtensionForm::class.java.getDeclaredField("extension")
        extensionField.set(plugin, wrapper)
        plugin.primaryStage = primaryStage
        val fieldsInitializedField = ExtensionForm::class.java.getDeclaredField("fieldsInitialized")
        val fieldsInitialized = fieldsInitializedField.get(plugin) as Observable<*>
        fieldsInitialized.fireEvent()
        thread(start = true, name = info.title) {
            try {
                ExtensionClient(args = args, plugin = plugin).run()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        Platform.setImplicitExit(false)
        primaryStage.setOnCloseRequest {
            it.consume()
            Platform.runLater {
                primaryStage.hide()
                plugin.onHide()
            }
        }
    }
}

inline fun <reified P : Plugin<*>> launch(vararg args: String) {
    val info = P::class.findAnnotation<PluginInfo>()
        ?:throw Exception("Missing PluginInfo annotation")
    PluginApplication.args = args.toList().toTypedArray()
    PluginApplication.info = info
    PluginApplication.creator = object : ExtensionFormCreator() {
        override fun createForm(stage: Stage): ExtensionForm {
            val loader = FXMLLoader(this::class.java.classLoader.getResource(info.fxmlFile))
            val root = loader.load<Parent>()
            stage.title = info.title
            stage.scene = Scene(root)
            stage.isResizable = info.resizeable
            stage.isAlwaysOnTop = info.alwaysOnTop
            try {
                stage.icons.add(Image(requireNotNull(Plugin::class.java.getResource(info.iconFile)) {
                    "Could not read icon from ${info.iconFile}"
                }.openStream()))
            } catch (e: Exception) {
                e.printStackTrace()
            }
            stage.sizeToScene()
            val p = P::class.createInstance()
            p.controller = loader.getController()
            return p
        }
    }
    Application.launch(PluginApplication::class.java, *args)
}
