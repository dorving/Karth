package dorving.karth.junit

import gearth.extensions.Extension
import gearth.extensions.ExtensionInfo

@ExtensionInfo(
    Title =  "Mockk Extension",
    Description =  "Used for testing the Karth API",
    Version =  "NA",
    Author =  "Stan"
)
class TestExtension : Extension(arrayOf("-p", "9092"))
