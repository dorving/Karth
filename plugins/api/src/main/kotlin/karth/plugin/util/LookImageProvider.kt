package karth.plugin.util

import javafx.scene.image.Image

object LookImageProvider {

    private const val BASE_URL = "https://www.habbo.com/habbo-imaging/avatarimage?size=m&figure="
    private val cache = HashMap<String, Image>()

    fun getOrFetchImage(imageString: String) =
        cache.getOrPut(imageString) { Image(BASE_URL + imageString) }
}
