package karth.core.api

@kotlinx.serialization.Serializable
enum class ProductType(val id: String) {

    WallItem("I"),
    FloorItem("S"),
    Effect("E"),
    Badge("B");

    override fun toString(): String = id

    companion object {
        fun fromString(id: String) = values().find { it.id.contentEquals(id, ignoreCase = true) }
    }
}