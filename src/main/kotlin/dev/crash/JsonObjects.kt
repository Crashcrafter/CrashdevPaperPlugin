package dev.crash

class CoingeckoPriceInfo(elements: Map<String, CoingeckoCurrencyValue>) : HashMap<String, CoingeckoCurrencyValue>(elements)

data class CoingeckoCurrencyValue (
    val usd: Double
)
