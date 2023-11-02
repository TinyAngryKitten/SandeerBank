package chatgpt

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.bodyAsText
import io.ktor.http.contentType
import io.ktor.http.headersOf
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import util.addBearerAuthHeader
import util.jsonContentType


class ChatGptClient(
    val secret: String,
    val endpoint: String = "https://api.openai.com/v1/",
): KoinComponent {
    private val client: HttpClient by inject()
    val nameRegex = Regex("\"name\"\\s?:\\s?\"(.*)\"")
    val categoryRegex = Regex("\"category\"\\s?:\\s?\"(.*)\"")

    //gpt-4 soon
    suspend fun sendChatRequest(message: String, model: String = "gpt-3.5-turbo"): ChatGptChatResponse =
        client.runCatching {
            post {
                url("$endpoint/chat/completions")
                contentType(jsonContentType)
                headers.apply(addBearerAuthHeader(secret))
                setBody(ChatGptChatRequest(
                    listOf(
                        ChatGptChatMessage(
                            ChatGptChatRole.user,
                            message
                        )
                    ),
                    model
                ))
            }
        }.also {
            if(it.isSuccess) println(it.getOrThrow().bodyAsText()) else it.exceptionOrNull()?.printStackTrace()
        }.getOrThrow().body()

    private fun createCategorizationMessage(creditorName: String, currency: String) =
        """
            I have one transaction in my transaction history with the description "$creditorName". 
            It could contain some irrelevant information, like a number or a non-ascii letter, 
            or the description could be incomplete, i made this transaction either in a shop or by ordering online.
            Can you help me select the name of the shop or company where it is most likely that i made this purchase at?.
            The purchase was made today in a country that uses the currency $currency. 
            The. The description could contain a prefix with the name of a payment provider,
            like Klarna or Vipps. Answer me with a json object, with the fields name, and category.
            The category field should be one of these values: Other, Restaurant, Electronics, Interior, Travel, Groceries.
            If the shop does not match a specific category, put it in the Other category.
        """.trimIndent()

    suspend fun categorizeTransaction(creditorName: String?, currency: String): Pair<String?, String?> =
        if(creditorName == null) Pair(null, null)
        else sendChatRequest(createCategorizationMessage(creditorName, currency))
            .choices.firstOrNull()
            ?.message?.content
            ?.let {
                Pair(
                    nameRegex.find(it)?.groupValues?.get(1),
                    categoryRegex.find(it)?.groupValues?.get(1),
                )
            } ?: Pair(null, null)
}