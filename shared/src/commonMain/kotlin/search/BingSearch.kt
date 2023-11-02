package search

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.encodeURLParameter
import io.ktor.http.encodeURLPath
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface SearchApi {
    suspend fun findPicture(query: String): ImageSearchAnswer?
}

class BingSearch(
    val secret: String,
    val market: String = "no-NO",
    val count: Int = 1
): SearchApi, KoinComponent {
    private val client: HttpClient by inject()
    private val endpoint = "https://api.bing.microsoft.com/"
    override suspend fun findPicture(query: String): ImageSearchAnswer? =
        try {
            client.get("$endpoint/v7.0/images/search?mkt=$market&count=$count&q=${query.encodeURLParameter()}") {
                header("Ocp-Apim-Subscription-Key", secret)
            }.also {
                println(it.bodyAsText())
            }.body()
        }catch (e: Exception) {
            e.printStackTrace()
            null
        }
}