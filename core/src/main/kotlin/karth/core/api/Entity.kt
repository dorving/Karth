package karth.core.api

@kotlinx.serialization.Serializable
sealed class Entity {

    abstract val id: Int
    abstract val name: String
    abstract val motto: String
    abstract val figureId: String
    abstract val tile: Point
    abstract val index: Int
    abstract val unknown1: Int
    abstract val entityType: EntityType

    @kotlinx.serialization.Serializable
    data class Habbo(
        override val id: Int,
        override val name: String,
        override val motto: String,
        override val figureId: String,
        override val index: Int,
        override val tile: Point,
        override val unknown1: Int,
        val gender : Gender,
        val arg1: Int,
        val arg2: Int,
        val favoriteGroup: String,
        val arg4: String,
        val arg5: Int,
        val arg6: Boolean
    ) : Entity() {
        override val entityType: EntityType = EntityType.HABBO
    }

    @kotlinx.serialization.Serializable
    data class Pet(
        override val id: Int,
        override val name: String,
        override val motto: String,
        override val figureId: String,
        override val index: Int,
        override val tile: Point,
        override val unknown1: Int,
        val arg0 : Int,
        val arg1 : Int,
        val arg2 : String,
        val arg3 : Int,
        val arg4 : Boolean,
        val arg5 : Boolean,
        val arg6 : Boolean,
        val arg7 : Boolean,
        val arg8 : Boolean,
        val arg9 : Boolean,
        val arg10 : Int,
        val arg11 : String
    ) : Entity() {
        override val entityType: EntityType = EntityType.PET
    }

    @kotlinx.serialization.Serializable
    data class LegacyBot(
        override val id: Int,
        override val name: String,
        override val motto: String,
        override val figureId: String,
        override val index: Int,
        override val tile: Point,
        override val unknown1: Int
    ) : Entity() {
        override val entityType: EntityType = EntityType.LEGACY_BOT
    }

    @kotlinx.serialization.Serializable
    data class Bot(
        override val id: Int,
        override val name: String,
        override val motto: String,
        override val figureId: String,
        override val index: Int,
        override val tile: Point,
        override val unknown1: Int,
        val arg0: String,
        val arg1: Int,
        val arg2: String,
        val arg4: List<Short>
    ) : Entity() {
        override val entityType: EntityType = EntityType.BOT
    }
} 
