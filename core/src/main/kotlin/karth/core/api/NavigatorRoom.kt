package karth.core.api

@kotlinx.serialization.Serializable
data class NavigatorRoom(
    val flatId: Int,
    val roomName: String,
    val ownerId: Int,
    val ownerName: String,
    val doorMode: Int,
    val userCount: Int,
    val maxUserCount: Int,
    val description: String,
    val tradeMode: Int,
    val score: Int,
    val ranking: Int,
    val categoryId: Int,
    val tags: Array<String>,
    val officialRoomPicRef: String? = null,
    val groupId: Int = -1,
    val groupName: String? = null,
    val groupBadgeCode: String? = null,
    val roomAdName: String? = null,
    val roomAdDescription: String? = null,
    val roomAdExpiresInMin: Int = -1,
    val showOwner: Boolean,
    val allowPets: Boolean,
    val displayRoomEntryAd: Boolean
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as NavigatorRoom

        if (flatId != other.flatId) return false

        return true
    }

    override fun hashCode(): Int {
        return flatId
    }
}