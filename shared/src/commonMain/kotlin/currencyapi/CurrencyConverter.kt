package currencyapi

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.request
import io.ktor.client.request.url
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpMethod
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface CurrencyConverter {
    suspend fun convert(to: String, from: String, amount: Float, date: String?): Float?
}

class CurrencyApi(val apiKey: String): CurrencyConverter, KoinComponent {
    private val client: HttpClient by inject()
    private val endpoint = "https://api.currencyapi.com"

    override suspend fun convert(to: String, from: String, amount: Float, date: String?): Float? =
        runCatching {
            getConvertionRate(to, from, amount, date)
                .data[from]
                ?.value?.times(amount)
        }.also {
            it.exceptionOrNull()?.printStackTrace()
        }.getOrNull()

    private suspend fun getConvertionRate(from: String, to: String, amount: Float, date: String?): HistoricalCurrencyResponse =
        runCatching {
            client.request {
                method = HttpMethod.Get
                url("$endpoint/v3/historical?base_currency=$from&currencies=$to${ if(date.isNullOrBlank()) "" else "&date=$date" }")
                header("apikey", apiKey)
            }
        }.also {
            if(it.isSuccess) {
                println("Status: ${it.getOrThrow().status.value}")
                println("Body: ${it.getOrThrow().bodyAsText()}")
            } else it.exceptionOrNull()?.printStackTrace()
        }.getOrThrow().body<HistoricalCurrencyResponse>()
}