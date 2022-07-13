# Karth
Karth is a wrapper around [GEarth](https://github.com/sirjonasxx/G-Earth) with as main goal to provide a type-safe way to listen for and send messages.

### Type-safe

Karth provides a type-safe way to listen for and send messages.
There are `Message.Outgoing` (to server) and `Message.Incoming` (to client) typed messages.
These are [sealed classes](https://kotlinlang.org/docs/sealed-classes.html) 
and define the specific `Message`implementations.

Below follows an example of Karth's [Message](core/src/main/kotlin/dorving/karth/message/ServerPacket.kt) implementation:

## Usage

### Using Karth Extension framework
If you want to use the Karth Plugin (Extension) framework. Make sure to add the following dependency:
```kts
// TODO: add dependency
```

See the [plugins.examples](plugins/examples) module for some sample plugins.

### Using other Extension framework
If you want to use Karth in a different Extension framework, 
create a new [KarthSession](core/src/main/kotlin/karth/core/KarthSession.kt).

```kt
val session = KarthSession(extension)
```
Listen to a message of a specific type during lifetime of program.
```kt
 // Listen for incoming messages of type Chat
session.on<Message.Incoming.Chat> {
    // access properties of Chat messages within lambda scope
    // behaves like `this` reference, could do `this.userIndex`
    println("$userIndex send $message")
}
```

Send a message of a specific type.
```kt
val heyChat = Message.Outgoing.Chat(userIndex = 69, "Hey", color = 1)
session.send(heyChat)
```

Send and expect a message, blocking the current thread until expected message is received, or when a timeout occurs.
```kt
 // Listen for the first Message.Incoming.Chat message after sending the packet
session.sendAndReceive<Outgoing.Chat, Incoming.Chat>(
    toSend = heyChat,
    onReceive = { println("Received a message from $userIndex containing $contents") }
)

// Add a condition, only accept the first message that fulfills it
session.sendAndReceive<Outgoing.Chat, Incoming.Chat>(
    toSend = heyChat,
    condition = { userIndex == 69 }, // accept first message for user with index `69`
    onReceive = { println("Received a message from $userIndex containing $contents") }
)

// Specify max duration of an attempt at receiving the expected packet
session.sendAndReceive<Outgoing.Chat, Incoming.Chat>(
    toSend = heyChat,        
    maxWaitTime = 1000.milliseconds,
    condition = { userIndex == 69 }, // accept first message for user with index `69`
    onReceive = { println("Received a message from $userIndex containing $contents") },
)

// Specify how many times the method should retry in case of a timeout/read failure.
session.sendAndReceive<Outgoing.Chat, Incoming.Chat>(
    toSend = heyChat,
    maxWaitTime = 1000.milliseconds,
    maxAttempts = 10, // if failed 10 times in a row, stop blocking
    condition = { userIndex == 69 }, // accept first message for user with index `69`
    onReceive = { println("Received a message from $userIndex containing $contents") },
)

// Handle exceptions
session.sendAndReceive<Outgoing.Chat, Incoming.Chat>(
    toSend = heyChat,
    maxWaitTime = 1000.milliseconds,
    maxAttempts = 10, // if failed 10 times in a row, stop blocking
    condition = { userIndex == 69 }, // accept first message for user with index `69`
    onReceive = { println("Received a message from $userIndex containing $contents") },
    onException = { it.printStackTrace() }
)
```

### Live Entities

These are entities with observable states whose state reflect the current state in the client. 
Live Entities maintain their state by listening to relevant incoming messages.

Create a [LiveRoom](plugins/api/src/main/kotlin/karth/plugin/entity/LiveRoom.kt) instance.
```kt
val liveRoom = LiveRoom(session)
```

For example, a [LiveRoom](plugins/api/src/main/kotlin/karth/plugin/entity/LiveRoom.kt) entity listens to all Room related packets in order to maintain its state. 
It uses these packets for one to maintain a list of all the furniture in the room. 
```kt
val fxFloorItemNodes = FXCollections.observableArrayList<Label>() // e.g. used as backing list for some ListView
liveRoom.floorItemList.addListener(ListChangeListener {
    Platform.runLater {
        fxFloorItemNodes.setAll(it.list.map { floorItem -> Label("FloorItem(id=${floorItem.id})") })
    }
})
```

