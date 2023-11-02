package nordigen

import kotlinx.serialization.Serializable

@Serializable
data class Requisition(
    val id: String,
    val created: String,
    val redirect: String,
    val ssn: String,
    val link: String,
    val account_selection: Boolean,
    val redirect_immediate: Boolean
)