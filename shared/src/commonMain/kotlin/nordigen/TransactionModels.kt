package nordigen

import kotlinx.serialization.Serializable


@Serializable
data class AccountTransactionResponse(
    val transactions: TransactionsResponse
)
@Serializable
data class TransactionsResponse(val booked: List<Transaction>, val pending: List<Transaction>)

@Serializable
data class Transaction(
    val transactionId: String,
    val entryReference: String,
    val bookingDate: String,
    val valueDate: String? = null,
    val transactionAmount: TransactionAmount,
    val creditorName: String? = null,
    val proprietaryBankTransactionCode: String,
    val internalTransactionId: String? = null,
    var accountName: String? = null, //assigned after serialization
    var bankName: String? = null, //assigned after serialization
)

@Serializable
data class TransactionAmount(val amount: Float, val currency: String)