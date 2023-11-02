package nordigen

import kotlinx.serialization.Serializable


@Serializable
data class BalancesResponse(val balances: List<Balance>)

@Serializable
data class Balance(
    val balanceAmount: BalanceAmount,
    val balanceType: String,
    val referenceDate: String
)

@Serializable
data class BalanceAmount(
    val amount: Float,
    val currency: String
)