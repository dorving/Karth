package karth.core.api

enum class EntityType(val id: Int) {

    HABBO(1),
    PET(2),
    LEGACY_BOT(3),
    BOT(4);

    companion object {
        fun forId(id: Int) = values().find { it.id == id }
    }
}