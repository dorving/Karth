package karth.core.api

@kotlinx.serialization.Serializable
data class InventoryItem(
    val placementId: Int,
    val type: ProductType,
    val id: Int,
    val typeId: Int,
    val specialType: SpecialType,
    val category: Int,
    val stuff: Stuff,
    val recyclable: Boolean,
    val tradeable: Boolean,
    val groupable: Boolean,
    val sellable: Boolean,
    val secondsToExpiration: Int,
    val rentPeriodStarted: Boolean,
    val roomId: Int,
    val slotId: String = "",
    val extra: Int = -1
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as InventoryItem

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int = id
}