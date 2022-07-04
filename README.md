# Karth
Karth is a wrapper around [GEarth](https://github.com/sirjonasxx/G-Earth) with as main goal to provide a type-safe way to listen for and send messages.

### Type-safe

Karth provides a type-safe way to listen for and send messages.
There are `Message.Outgoing` (to server) and `Message.Incoming` (to client) typed messages.
These are [sealed classes](https://kotlinlang.org/docs/sealed-classes.html) 
and define the specific `Message`implementations.

Below follows an example of Karth's [Message](src/main/kotlin/dorving/karth/message/Message.kt) implementation:
```kt
sealed class Message(val name: String, val direction: HMessage.Direction) {
    
    sealed class Incoming(name: String) : Message(name, HMessage.Direction.TOCLIENT) {

        @Decoder(MessageDecoder.IncomingChatDecoder::class)
        data class Chat(val userIndex: Int, val message: String) : Incoming("Chat")
    }
   
    sealed class Outgoing(name: String) : Message(name, HMessage.Direction.TOSERVER) {

        @Encoder(MessageEncoder.OutgoingChatEncoder::class)
        data class Chat(val message: String, val colorIndex: Int, val userIndex: Int) : Outgoing("Chat")
    }
}
```

## Usage

To use Karth, create a new [KarthSession](src/main/kotlin/dorving/karth/KarthSession.kt).

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

// Handle exceptions
session.sendAndReceive<Outgoing.Chat, Incoming.Chat>(
    toSend = heyChat,
    onException = { it.printStackTrace() },
    onReceive = { println("Received a message from $userIndex containing $contents") },
)
```
