package dorving.karth.message

fun interface MessageListener {

    fun onMessage(message: Message): Response

    enum class Response {
        CONTINUE,
        REMOVE
    }
}
