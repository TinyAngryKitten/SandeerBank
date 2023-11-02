package db

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import org.mongodb.kbson.ObjectId

@Serializable
enum class TransactionCategory() {
    Groceries,
    Restaurant,
    Electronics,
    Interior,
    Travel,
    Other;

    // Icons under public domain
    val iconFile: String = "xml/$name.xml"
}

@Serializable
class ProcessedTransaction() : RealmObject {
    @PrimaryKey
    var _id: ObjectId = ObjectId()
    var processedMerchantName: String? = null
    var rawMerchantName: String? = null
    var transactionId: String = ""
    var amount: Float = 0f
    var processedAmount: Float? = null
    var currency: String = ""
    var bookingTimestamp: String? = null
    var imageUrl: String? = null
    val bookingDate: LocalDate?
        get() = try { LocalDate.parse(bookingTimestamp!!) } catch(_: Exception) { null }
    var valueTimestamp: String? = null
    val valueDate: LocalDate?
        get() = try { LocalDate.parse(valueTimestamp!!) } catch(_: Exception) { null }
    var accountName: String? = null
    var bankName: String? = null
    var categoryName: String? = null
    var category: TransactionCategory? //Realm can not serialize enums
        get() = try { categoryName?.let(TransactionCategory::valueOf) } catch (_: Exception) { null }
        set(value) { categoryName = value?.name }

    fun isOfCategory(category: TransactionCategory) =
        category == (this.category ?: TransactionCategory.Other)

    constructor(
        processedMerchantName: String? = null,
        rawMerchantName: String? = null,
        transactionId: String = "",
        amount: Float = 0f,
        currency: String = "",
        bookingTimestamp: String? = null,
        valueTimestamp: String? = null,
        accountName: String? = null,
        bankName: String? = null,
        categoryName: String? = null,
        processedAmount: Float? = null,
        imageUrl: String? = null
    ): this() {
        this.processedMerchantName = processedMerchantName
        this.rawMerchantName = rawMerchantName
        this.transactionId = transactionId
        this.amount = amount
        this.currency = currency
        this.bookingTimestamp = bookingTimestamp
        this.valueTimestamp= valueTimestamp
        this.accountName = accountName
        this.bankName = bankName
        this.categoryName = categoryName
        this.processedAmount = processedAmount
        this.imageUrl = imageUrl
    }
}