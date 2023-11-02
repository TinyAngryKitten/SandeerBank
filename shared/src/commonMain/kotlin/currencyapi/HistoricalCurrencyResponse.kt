package currencyapi

import kotlinx.serialization.Serializable

@Serializable
class HistoricalCurrencyResponse(
    val meta: Map<String, String>,
    val data: Map<String, CurrencyRate>
)