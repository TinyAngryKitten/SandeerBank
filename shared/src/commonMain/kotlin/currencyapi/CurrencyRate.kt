package currencyapi

import kotlinx.serialization.Serializable

@Serializable
data class CurrencyRate(
    val code: String,
    val value: Float
)