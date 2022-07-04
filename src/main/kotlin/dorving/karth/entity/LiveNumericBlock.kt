package dorving.karth.entity

import dorving.karth.KarthSession
import dorving.karth.message.Message.Incoming.ObjectDataUpdate
import dorving.karth.message.Message.Outgoing.UseFurniture
import gearth.extensions.parsers.HFloorItem
import tornadofx.intProperty
import tornadofx.onChange
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class LiveNumericBlock(session: KarthSession, floorItem: HFloorItem) : LiveFloorItem(session, floorItem) {

    private val valueProperty = intProperty((floorItem.stuff[0] as String).toInt())

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
                    session.sendAndExpectMap<UseFurniture, ObjectDataUpdate, Unit>(UseFurniture(id),
                        onException = { lock.countDown() },
                        condition = { floorItemId == id },
                        handler = {
                            done.set(value == readValue())
                            lock.countDown()
                        }
                    )
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
