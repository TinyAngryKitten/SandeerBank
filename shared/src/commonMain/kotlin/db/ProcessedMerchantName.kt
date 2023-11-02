package db

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import kotlinx.serialization.Serializable
import org.mongodb.kbson.BsonObjectId
import org.mongodb.kbson.ObjectId

@Serializable
class ProcessedMerchantName(
    @PrimaryKey
    var rawName: String,
    var processedName: String
): RealmObject {
    constructor() : this(rawName = "", processedName = "")
}