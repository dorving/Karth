package karth.plugin.entity

import gearth.protocol.connection.HClient
import karth.core.KarthSession
import karth.core.api.Offers
import karth.core.message.Message.Incoming.*
import karth.core.message.Message.Outgoing.*
import tornadofx.objectProperty

class LiveMarketPlace(private val session: KarthSession) {

    var searchResultProperty = objectProperty<Offers.All>()
    var ownOffersProperty = objectProperty<Offers.Own>()

    init {
        session.onEach<GetOffersResult> { searchResultProperty.set(offers) }
        session.onEach<GetOwnOffersResult> { ownOffersProperty.set(offers) }
    }

    fun search(minPrice: Int, maxPrice: Int, searchTerm: String, sortType: SortOrder) =
        session.send(GetOffers(minPrice, maxPrice, searchTerm, sortType.encode()))

    fun updateOwnOffers() =
        session.send(GetOwnOffers)

    fun listOne(furniType: Int, sellPrice: Int, offerId: Int) =
        session.sendAndReceive<MakeOffer, MakeOfferResult>(MakeOffer.Single(furniType, sellPrice, offerId))

    fun listMany(furniType: Int, sellPrice: Int, vararg offerIds: Int): Map<Int, MakeOfferResult?> =
        when (val client = session.client) {
            HClient.FLASH ->
                offerIds.associateWith { listOne(furniType, sellPrice, it) }
            HClient.UNITY -> {
                val result = session.sendAndReceive<MakeOffer, MakeOfferResult>(
                    toSend = MakeOffer.Multi(furniType, sellPrice, offerIds)
                )
                offerIds.associateWith { result }
            }
            else -> throw Exception("Unsupported client $client")
        }

    fun cancel(offerId: Int) =
        session.send(CancelOffer(offerId))

    fun cancelAll() = when(val client = session.client) {
        HClient.UNITY -> session.send(CancelAllOffers)
        else -> throw Exception("Unsupported client $client")
    }

    fun buy(offerId: Int) =
        session.send(BuyOffer(offerId))
    enum class SortOrder {
        MOST_EXPENSIVE_FIRST,
        CHEAPEST_FIRST,
        MOST_TRADED_FIRST,
        LEAST_TRADED_FIRST,
        MOST_OFFERS_FIRST,
        LEAST_OFFERS_FIRST;
        fun encode() = ordinal + 1
    }
}
