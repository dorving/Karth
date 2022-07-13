package karth.launcher

import gearth.GEarth
import gearth.ui.GEarthController

object KarthLauncher {

    @JvmStatic
    fun main(args: Array<String>) {
        val field = GEarth::class.java.getDeclaredField("controller")
        field.isAccessible = true
        val appThread = Thread {
            GEarth.main(args)
        }
        appThread.start()
        println("Waiting for controller")

        Thread.sleep(4000L)
        val controller = field.get(GEarth.main) as GEarthController
        println("Obtained controller")
        controller.connectionController.rd_flash.fire()
        controller.connectionController.run {
            rd_flash.fire()
            cbx_autodetect.selectedProperty().set(true)
            btnConnect.fire()
        }
        appThread.join()
    }
}
