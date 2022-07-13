package karth.core.api

@kotlinx.serialization.Serializable
data class Point(
    val x: Int = 0,
    val y: Int = 0,
    val z: Double = 0.0
)