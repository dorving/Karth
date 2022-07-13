package karth.core.api

@kotlinx.serialization.Serializable
data class NavigatorBlock(
    val searchCode: String,
    val text: String,
    val actionAllowed: Int,
    val isForceClosed: Boolean,
    val viewMode: Int,
    val rooms: List<NavigatorRoom>
)