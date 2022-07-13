package karth.plugin.entity
import karth.core.KarthSession
import karth.core.api.FloorItem
import karth.core.message.Message.Incoming.ObjectDataUpdate
import karth.core.message.Message.Outgoing.UseFurniture
import tornadofx.intProperty
import tornadofx.onChange
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class LiveNumericBlock(session: KarthSession, floorItem: FloorItem) : LiveFloorItem(session, floorItem) {

    val valueProperty = intProperty((floorItem.stuff[0] as String).toInt())

    init {
        stuffList.onChange {
            valueProperty.set((it.list[0] as String).toInt())
        }
    }

    fun getValue() = (stuffList[0] as String).toInt()

    fun setValue(value: Int) {
        assert(value in 0..9) {
            "Value must be in range 0..9 but was $value"
        }
        println("value = [${value}] -> ${getValue()}")
        var maxTries = 10
        while(maxTries-- > 0) {
            val done = AtomicBoolean(getValue() == value)
            if (!done.get()) {
                val lock = CountDownLatch(1)
                try {
                    val update = session.sendAndReceive<UseFurniture, ObjectDataUpdate>(UseFurniture(id),
                        onException = { lock.countDown() },
                        condition = { floorItemId == id },
                    )!!
                    done.set(value == update.readValue())
                    lock.countDown()
                } catch (e: Exception) {
                    e.printStackTrace()
                    lock.countDown()
                }
                lock.await(10, TimeUnit.SECONDS)
            }
            if (done.get())
                break
        }
    }

    private fun ObjectDataUpdate.readValue() = (data[0] as String).toInt()
}
