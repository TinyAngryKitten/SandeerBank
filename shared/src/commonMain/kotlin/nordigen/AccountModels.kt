package nordigen

import kotlinx.serialization.Serializable

@Serializable
data class AccountResponse(
    val account: Account
)

@Serializable
data class Account(
    val resourceId: String,
    val iban: String,
    val currency: String,
    val ownerName: String,
    val name: String,
    val product: String,
    val cashAccountType: String
)