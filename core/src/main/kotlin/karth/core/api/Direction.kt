package karth.core.api

@kotlinx.serialization.Serializable
enum class Direction {
    North,
    NorthEast,
    East,
    SouthEast,
    South,
    SouthWest,
    West,
    NorthWest,
}