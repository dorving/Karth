package karth.core.message

fun interface MessageListener {

    fun onMessage(message: Message): Response

    enum class Response {
        CONTINUE,
        REMOVE
    }
}
