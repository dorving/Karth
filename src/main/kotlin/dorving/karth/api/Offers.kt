package dorving.karth.api

import gearth.extensions.parsers.HStuff
import gearth.protocol.HPacket
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.serialization.ExperimentalSerializationApi
import kotlin.time.ExperimentalTime

@ExperimentalTime
@DelicateCoroutinesApi
@ExperimentalSerializationApi
sealed interface Offers {

    val count: Int
    val floorFurni: MutableList<Offer.Floor>
    val wallFurni: MutableList<Offer.Wall>
    val limitedEditionFurni: MutableList<Offer.LimitedEdition>

    fun all() = floorFurni + wallFurni + limitedEditionFurni

    data class Own(
        override val count: Int,
        override val floorFurni: MutableList<Offer.Floor>,
        override val wallFurni: MutableList<Offer.Wall>,
        override val limitedEditionFurni: MutableList<Offer.LimitedEdition>,
    ) : Offers {

        companion object {
            fun parse(packet: HPacket): Own {

                val floorOffers = mutableListOf<Offer.Floor>()
                val wallOffers = mutableListOf<Offer.Wall>()
                val limitedEditionOffers = mutableListOf<Offer.LimitedEdition>()

                packet.readInteger()
                val offersCount = packet.readInteger()

                for (i in 0 until offersCount) {
                    val offerId = packet.readInteger()
                    val status = packet.readInteger()
                    val furniType = packet.readInteger()
                    val extra: List<Any>
                    when (furniType) {
                        1 -> {
                            val furniId = packet.readInteger()
                            val stuffCategory = packet.readInteger()
                            val stuffData = HStuff.readData(packet, stuffCategory)
                            extra = listOf(furniId, stuffCategory, stuffData)
                        }
                        2 -> {
                            val furniId = packet.readInteger()
                            val extraData = packet.readString()
                            extra = listOf(furniId, extraData)
                        }
                        3 -> {
                            val furniId = packet.readInteger()
                            val uniqueSerialNumber = packet.readInteger()
                            val uniqueSeriesSize = packet.readInteger()
                            extra = listOf(furniId, uniqueSerialNumber, uniqueSeriesSize)
                        }
                        else -> throw Exception("Read invalid furni type $furniType in packet for offer $offerId")
                    }
                    val price = packet.readInteger()
                    val timeLeftMinutes = packet.readInteger()
                    val averagePrice = packet.readInteger()
                    val offerCount = 1
                    when (furniType) {
                        1 -> {
                            floorOffers += Offer.Floor(
                                offerId,
                                status,
                                furniType,
                                price,
                                timeLeftMinutes,
                                averagePrice,
                                offerCount,
                                furniId = extra[0] as Int,
                                stuffCategory = extra[1] as Int
                            )
                        }
                        2 -> {
                            wallOffers += Offer.Wall(
                                offerId,
                                status,
                                furniType,
                                price,
                                timeLeftMinutes,
                                averagePrice,
                                offerCount,
                                furniId = extra[0] as Int,
                                inscription = extra[1] as String
                            )
                        }
                        3 -> {
                            limitedEditionOffers += Offer.LimitedEdition(
                                offerId,
                                status,
                                furniType,
                                price,
                                timeLeftMinutes,
                                averagePrice,
                                offerCount,
                                furniId = extra[0] as Int,
                                uniqueSerialNumber = extra[1] as Int,
                                uniqueSeriesSize = extra[2] as Int
                            )
                        }
                    }
                }
                return Own(
                    offersCount,
                    floorOffers,
                    wallOffers,
                    limitedEditionOffers
                )
            }
        }
    }

    data class All(
        override val count: Int,
        override val floorFurni: MutableList<Offer.Floor>,
        override val wallFurni: MutableList<Offer.Wall>,
        override val limitedEditionFurni: MutableList<Offer.LimitedEdition>,
    ) : Offers {

        companion object {
            fun parse(packet: HPacket): All {

                val floorOffers = mutableListOf<Offer.Floor>()
                val wallOffers = mutableListOf<Offer.Wall>()
                val limitedEditionOffers = mutableListOf<Offer.LimitedEdition>()

                val offersCount = packet.readInteger()

                for (i in 0 until offersCount) {
                    val offerId = packet.readInteger()
                    val status = packet.readInteger()
                    val furniType = packet.readInteger()
                    val extra: List<Any>
                    when (furniType) {
                        1 -> {
                            val furniId = packet.readInteger()
                            val stuffCategory = packet.readInteger()
                            val stuffData = HStuff.readData(packet, stuffCategory)
                            extra = listOf(furniId, stuffCategory, stuffData)
                        }
                        2 -> {
                            val furniId = packet.readInteger()
                            val extraData = packet.readString()
                            extra = listOf(furniId, extraData)
                        }
                        3 -> {
                            val furniId = packet.readInteger()
                            val uniqueSerialNumber = packet.readInteger()
                            val uniqueSeriesSize = packet.readInteger()
                            extra = listOf(furniId, uniqueSerialNumber, uniqueSeriesSize)
                        }
                        else -> throw Exception("Read invalid furni type $furniType in packet for offer $offerId")
                    }
                    val price = packet.readInteger()
                    val timeLeftMinutes = packet.readInteger()
                    val averagePrice = packet.readInteger()
                    val offerCount = packet.readInteger()
                    when (furniType) {
                        1 -> {
                            floorOffers += Offer.Floor(
                                offerId,
                                status,
                                furniType,
                                price,
                                timeLeftMinutes,
                                averagePrice,
                                offerCount,
                                furniId = extra[0] as Int,
                                stuffCategory = extra[1] as Int
                            )
                        }
                        2 -> {
                            wallOffers += Offer.Wall(
                                offerId,
                                status,
                                furniType,
                                price,
                                timeLeftMinutes,
                                averagePrice,
                                offerCount,
                                furniId = extra[0] as Int,
                                inscription = extra[1] as String
                            )
                        }
                        3 -> {
                            limitedEditionOffers += Offer.LimitedEdition(
                                offerId,
                                status,
                                furniType,
                                price,
                                timeLeftMinutes,
                                averagePrice,
                                offerCount,
                                furniId = extra[0] as Int,
                                uniqueSerialNumber = extra[1] as Int,
                                uniqueSeriesSize = extra[2] as Int
                            )
                        }
                    }
                }
                return All(
                    offersCount,
                    floorOffers,
                    wallOffers,
                    limitedEditionOffers
                )
            }
        }
    }
}
