package karth.core.api

@kotlinx.serialization.Serializable
data class FloorItem(
    var id: Int = 0,
    val typeId: Int = 0,
    val tile: Point,
    val facing: Direction,
    val category: Int = 0,
    val secondsToExpiration: Int = 0,
    val usagePolicy: Int = 0,
    val ownerId: Int = 0,
    val ownerName: String?,
    var stuff: Stuff,
    val ignore1: String,
    val ignore2: Int,
    val ignore3: String?
) {

    val x by tile::x
    val y by tile::y
    val z by tile::z
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FloorItem

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int = id
}