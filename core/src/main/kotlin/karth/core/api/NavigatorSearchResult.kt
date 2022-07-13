package karth.core.api


@kotlinx.serialization.Serializable
data class NavigatorSearchResult(
    val searchCode: String,
    val filteringData: String,
    val blocks: List<NavigatorBlock>
)