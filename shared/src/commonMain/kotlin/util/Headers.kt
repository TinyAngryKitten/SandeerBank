package util

import io.ktor.http.ContentType
import io.ktor.http.HeadersBuilder

val jsonContentType = ContentType.parse("application/json")
val addBearerAuthHeader: (String) -> HeadersBuilder.() -> Unit = { token ->
    {
        append("Authorization", "Bearer $token")
    }
}