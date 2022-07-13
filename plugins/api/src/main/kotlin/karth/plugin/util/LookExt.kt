package karth.plugin.util

import karth.core.api.Look

fun Look.getOrFetchImage() = LookImageProvider.getOrFetchImage(figureString)
