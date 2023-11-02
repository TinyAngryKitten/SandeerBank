package nordigen

import kotlinx.serialization.Serializable

@Serializable
data class RequisitionsResponse(
    val id: String,
    val status: String,
    val agreements: String,
    val accounts: List<String>,
    val reference: String
)