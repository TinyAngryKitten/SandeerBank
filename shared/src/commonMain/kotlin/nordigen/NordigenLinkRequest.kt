package nordigen

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@Serializable
data class NordigenLinkRequest(
    @SerialName("institution_id")
    val institutionId: String,
    val redirect: String
)

@Serializable
data class NordigenLinkResponse(
    val id: String,
    val redirect: String,
    val link: String
)