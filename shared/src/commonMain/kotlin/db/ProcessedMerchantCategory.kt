package db

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import kotlinx.serialization.Serializable
import org.mongodb.kbson.BsonObjectId
import org.mongodb.kbson.ObjectId

@Serializable
class ProcessedMerchantCategory(
    @PrimaryKey
    var rawName: String,
    var category: String
): RealmObject {
    constructor() : this(rawName = "", category = TransactionCategory.Other.name)
}