package dorving.karth.api

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.serialization.ExperimentalSerializationApi
import kotlin.time.ExperimentalTime

@ExperimentalTime
@DelicateCoroutinesApi
@ExperimentalSerializationApi
@kotlinx.serialization.Serializable
sealed class Offer {

    abstract val furniId: Int
    abstract val offerId: Int
    abstract val status: Int
    abstract val furniType: Int
    abstract val price: Int
    abstract val timeLeftMinutes: Int
    abstract val averagePrice: Int
    abstract val offerCount: Int

    fun isExpired() = timeLeftMinutes <= 0

    data class Floor(
        override val offerId: Int,
        override val status: Int,
        override val furniType: Int,
        override val price: Int,
        override val timeLeftMinutes: Int,
        override val averagePrice: Int,
        override val offerCount: Int,
        override val furniId: Int,
        val stuffCategory: Int,
    ) : Offer()

    data class Wall(
        override val offerId: Int,
        override val status: Int,
        override val furniType: Int,
        override val price: Int,
        override val timeLeftMinutes: Int,
        override val averagePrice: Int,
        override val offerCount: Int,
        override val furniId: Int,
        val inscription: String,
    ) : Offer()

    data class LimitedEdition(
        override val offerId: Int,
        override val status: Int,
        override val furniType: Int,
        override val price: Int,
        override val timeLeftMinutes: Int,
        override val averagePrice: Int,
        override val offerCount: Int,
        override val furniId: Int,
        val uniqueSerialNumber: Int,
        val uniqueSeriesSize: Int,
    ) : Offer()

}
