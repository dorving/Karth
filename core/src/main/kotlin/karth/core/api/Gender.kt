package karth.core.api

@kotlinx.serialization.Serializable
enum class Gender(val id: String) {

    Unisex("U"),
    Male("M"),
    Female("F");

    companion object {
        fun fromString(string: String) = values()
            .find { it.id.contentEquals(string, ignoreCase = true) }
    }
}
