package db

import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.UpdatePolicy
import io.realm.kotlin.ext.query
import io.realm.kotlin.query.find

class TransactionDatabase {
    private val config = RealmConfiguration.create(schema = setOf(
        ProcessedTransaction::class,
        ProcessedMerchantName::class,
        ProcessedMerchantCategory::class
    ))
    private lateinit var realm: Realm

    init {
        Realm.deleteRealm(config)
        realm = Realm.open(config)
    }

    suspend fun setProcessedMerchantName(rawName: String, processedName: String) =
        realm.write {
            val result = copyToRealm(ProcessedMerchantName(rawName = rawName, processedName = processedName), UpdatePolicy.ALL)
            query<ProcessedTransaction>()
                .find {
                    it.query("${ProcessedTransaction::rawMerchantName.name} == $0", rawName)
                        .find()
                        .forEach {
                            it.processedMerchantName = processedName
                        }
                }
            result
        }

    fun getProcessedMerchantName(rawName: String) =
        realm.query<ProcessedMerchantName>("${ProcessedMerchantName::rawName.name} == $0", rawName)
            .first()
            .find()

    suspend fun setProcessedMerchantCategory(rawName: String, category: TransactionCategory) =
        realm.write {
            val result = copyToRealm(ProcessedMerchantCategory(rawName = rawName, category = category.name), UpdatePolicy.ALL)
            query<ProcessedTransaction>()
                .find {
                    it.query("${ProcessedTransaction::rawMerchantName.name} == $0", rawName)
                        .find()
                        .forEach {
                            it.category = category
                        }
                }
            result
        }

    fun getProcessedMerchantCategory(rawName: String) =
        realm.query<ProcessedMerchantCategory>("${ProcessedMerchantName::rawName.name} == $0", rawName)
            .first()
            .find()

    suspend fun addTransaction(transaction: ProcessedTransaction) =
        realm.write {
            copyToRealm(transaction)
            commit
        }

    suspend fun updateCategoryForAll(merchantName: String, transactionCategory: TransactionCategory) =
        realm.write {
            query<ProcessedTransaction>()
                .find {
                    it.query("${ProcessedTransaction::rawMerchantName.name} == $0", merchantName)
                        .find()
                        .forEach {
                            it.category = transactionCategory
                        }
                }
        }

    fun fetchTransactions() =
        realm.query<ProcessedTransaction>().find()

    fun exists(transactionId: String) =
        realm.query<ProcessedTransaction>("${ProcessedTransaction::transactionId.name} == $0", transactionId)
        .find()
        .isNotEmpty()
}