package dev.crash

data class SpawnObj(
    val x: Double,
    val y: Double,
    val z: Double,
    val world: String
)

class CoingeckoPriceInfo(elements: Map<String, CoingeckoCurrencyValue>) : HashMap<String, CoingeckoCurrencyValue>(elements)

data class CoingeckoCurrencyValue (
    val usd: Double
)
