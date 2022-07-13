package karth.plugin.entity

import javafx.collections.FXCollections
import karth.core.KarthSession
import karth.core.api.Look
import karth.core.message.Message.Incoming
import karth.core.message.Message.Outgoing
import tornadofx.objectProperty

class LiveWardrobe(private val session: KarthSession) {

    val savedLooks = FXCollections.observableArrayList<Look>()
    val hotLooks = FXCollections.observableArrayList<Look>()
    var currentLookProperty = objectProperty<Look>()

    init {
        session.onEach<Incoming.GetWardrobeResult> {
            savedLooks.setAll(lookEntries.map { it.look })
        }
        session.onEach<Incoming.GetHotLooksResult> {
            hotLooks.setAll(looks)
        }
        session.onEach<Incoming.GetNFTWardrobeResult> {
            // TODO: add support for NFT wardrobe
        }
        session.onEach<Incoming.FigureUpdate> {
            currentLookProperty.set(look)
        }
    }

    fun refreshLooks() {
        session.send(Outgoing.GetWardrobe)
        session.send(Outgoing.GetNFTWardrobe)
        session.send(Outgoing.GetHotLooks())
    }

    fun updateLook() {
        session.send(Outgoing.UpdateFigureData(look = currentLookProperty.get()))
    }
}
