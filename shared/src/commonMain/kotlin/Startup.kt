import chatgpt.ChatGptClient
import currencyapi.CurrencyConverter
import db.ProcessedMerchantCategory
import db.ProcessedMerchantName
import db.ProcessedTransaction
import db.TransactionCategory
import db.TransactionDatabase
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import nordigen.NordigenClient
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import search.SearchApi

class Startup: KoinComponent {
    private val nordigen: NordigenClient by inject()
    private val chatGpt: ChatGptClient by inject()
    private val db: TransactionDatabase by inject()
    private val currencyConverter: CurrencyConverter by inject()
    private val search: SearchApi by inject()

    suspend fun fetchAndProcessTransactions(accountId: String, bankName: String?) =
        nordigen
            .fetchTransactions(accountId)
            .transactions.booked
            .filterNot { db.exists(it.transactionId) }
            .forEach {
                try {
                    var name: ProcessedMerchantName? = db.getProcessedMerchantName(it.creditorName ?: "")
                    var category: ProcessedMerchantCategory? = db.getProcessedMerchantCategory(it.creditorName ?: "")
                    if(name == null || category == null && it.creditorName != null) {
                        val (newName, newCategory) = chatGpt.categorizeTransaction(it.creditorName, it.transactionAmount.currency)
                        if(newName != null) {
                            name = db.setProcessedMerchantName(it.creditorName!!, newName)
                        }
                        if(newCategory != null) {
                            category = db.setProcessedMerchantCategory(
                                it.creditorName!!,
                                TransactionCategory.valueOf(newCategory)
                            )
                        }
                    }

                    val processedAmount = it.transactionAmount.run {
                        if(!currency.equals("NOK", true))
                            currencyConverter.convert("NOK", currency, amount, it.valueDate)
                        else amount
                    }

                    val imageUrl = name?.processedName?.let { search.findPicture(it)?.value?.first()?.contentUrl }

                    val transaction = ProcessedTransaction(
                        processedMerchantName = name?.processedName,
                        rawMerchantName = it.creditorName,
                        categoryName = category?.category,
                        transactionId = it.transactionId,
                        amount = it.transactionAmount.amount,
                        processedAmount = processedAmount,
                        currency = it.transactionAmount.currency,
                        bookingTimestamp = it.bookingDate,
                        valueTimestamp = it.valueDate,
                        accountName = it.accountName,
                        bankName = bankName,
                        imageUrl = imageUrl
                    )

                    db.addTransaction(transaction)
                } catch(e: Exception) {
                    e.printStackTrace()
                }
        }
}