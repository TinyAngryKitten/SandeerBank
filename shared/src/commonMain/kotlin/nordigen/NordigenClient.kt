package nordigen

import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.accept
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.parameters
import io.ktor.util.reflect.TypeInfo
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.serialization.Serializable
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import util.addBearerAuthHeader
import util.jsonContentType

//{"id":"KOMPLETT_NDEANOKK","name":"Komplett Bank","bic":"NDEANOKK","transaction_total_days":"730","countries":["NO"],"logo":"https://cdn.nordigen.com/ais/KOMPLETT_NDEANOKK.png"}
private val banks = listOf(
    "MORROW_NDEANOKK"
)
private val accounts = listOf(
    "53f70d81-348b-45b0-920c-df46d44197b2"
)

//https://nordigen.com/en/account_information_documenation/integration/quickstart_guide/
private val requisitionId = "877ed808-f8da-44f2-9fe9-b68743f11dfc"//fra autorisasjons url
val accountId = "95c731d8-1761-447b-9e4e-6c5dc6f5d56c"
private fun accountUrl(account: String) = "https://bankaccountdata.gocardless.com/api/v2/accounts/$account"
private fun transactionUrl(account: String) = "https://bankaccountdata.gocardless.com/api/v2/accounts/$account/transactions/"
private fun balanceUrl(account: String) = "https://bankaccountdata.gocardless.com/api/v2/accounts/$account/balances/"
private fun requisitionsUrl(requisition: String) = "https://bankaccountdata.gocardless.com/api/v2/requisitions/$requisition/"
///api/v2/accounts/{id}/details/
@Serializable
data class NordigenAccessToken(val access: String)

class NordigenClient(
    val endpoint: String,
    val secret: String,
    val secretId: String
): KoinComponent {
    private val client: HttpClient by inject()
    private val settings: Settings by inject()
    private val scope = CoroutineScope(Dispatchers.Default)
    private var token: Deferred<String> = scope.async {
        val storedToken = settings.getStringOrNull("token")
        if (storedToken == null) {
            val newToken = fetchAccessToken().access
            settings["token"] = newToken
            newToken
        } else storedToken
    }

    private suspend fun updateAccessToken() {
        val newToken = fetchAccessToken().access
        settings["token"] = newToken
        token = CompletableDeferred(newToken)
    }

    suspend fun fetchNewRequisitionLink(
        bank: String,
        redirect: String = "https://localhost"
    ): NordigenLinkResponse =
        request {
            method = HttpMethod.Post
            accept(jsonContentType)
            contentType(jsonContentType)
            setBody(NordigenLinkRequest(bank, redirect))
            url("https://bankaccountdata.gocardless.com/api/v2/requisitions/")
        }.body()

    suspend fun fetchRequisitionAccounts(requisitionId: String): RequisitionsResponse =
        request {
            accept(jsonContentType)
            url(requisitionsUrl(requisitionId))
        }.body()

    private suspend fun request(builder: HttpRequestBuilder.() -> Unit) =
        client.runCatching {
            request {
                headers.apply(addBearerAuthHeader(token.await()))
                builder()
            }.let {
                if (it.status == HttpStatusCode.Unauthorized) {
                    updateAccessToken()
                    client.request {
                        builder()
                        headers.apply(addBearerAuthHeader(token.await()))
                    }
                } else it
            }
        }.also {
            if(it.isSuccess) println(it.getOrThrow().bodyAsText()) else it.exceptionOrNull()?.printStackTrace()
        }.getOrThrow()

    private suspend fun fetchAccessToken(): NordigenAccessToken =
        client.submitForm(formParameters = parameters {
            append("secret_id", secretId)
            append("secret_key", secret)
        }){
            url("https://bankaccountdata.gocardless.com/api/v2/token/new/")
            accept(jsonContentType)
            contentType(jsonContentType)
        }.body()

    suspend fun fetchBalances(): List<BalancesResponse> =
        accounts.map { fetchBalance(it) }

    private suspend fun fetchBalance(account: String): BalancesResponse =
        request {
            url(balanceUrl(account))
            accept(jsonContentType)
            contentType(jsonContentType)
        }.body()

    /*suspend fun fetchRequisitionUrl(bank: String): Requisition {
        val requestBody = FormBody.Builder()
            .add("institution_id", bank)
            .add("redirect", "http://localhost")
            .build()

        val response = suspendCoroutine{ continuation ->
            Request.Builder()
                .url("https://ob.nordigen.com/api/v2/requisitions/")
                .addHeader("accept", "application/json")
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer $token")
                .post(requestBody)
                .build()
                .let(client::newCall)
                .enqueue(callback(continuation))
        }
        val body = response.body!!.string()
        return Gson().fromJson(body, Requisition::class.java)
    }*/

    suspend fun fetchAccount(accountId: String): AccountResponse =
        request {
            method = HttpMethod.Get
            url(accountUrl(accountId))
            accept(jsonContentType)
        }.body()

    suspend fun fetchTransactions(accountId: String): AccountTransactionResponse =
        try {
            request {
                url(transactionUrl(accountId))
                accept(jsonContentType)
            }.also {
                println(
                    it.bodyAsText()
                )
            }.body()
        } catch (e: Exception) {
            e.printStackTrace()
            AccountTransactionResponse(TransactionsResponse(listOf(), listOf()))
        }
}