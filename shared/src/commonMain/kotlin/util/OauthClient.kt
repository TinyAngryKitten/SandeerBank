package util

import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.accept
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.url
import io.ktor.client.statement.bodyAsText
import io.ktor.http.contentType
import io.ktor.http.parameters
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import nordigen.NordigenAccessToken
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class OauthClient(
    val secret: String,
    val secretId: String,
    val tokenEndpoint: String
): KoinComponent {
    private val settings: Settings by inject()
    private val client: HttpClient by inject()
    private val scope = CoroutineScope(Dispatchers.Default)
    private var token: Deferred<String> = scope.async {
        val storedToken = settings.getStringOrNull(secretId)
        if(storedToken == null) {
            val newToken = fetchAccessToken().access
            settings[secretId] = newToken
            newToken
        } else storedToken
    }

    private suspend fun updateAccessToken() {
        token = scope.async { fetchAccessToken().access }.also {
            settings[secretId] = it.await()
        }
        token.await()
    }

    private suspend fun fetchAccessToken(): NordigenAccessToken =
        client.submitForm(formParameters = parameters {
            append("secret_id", secretId)
            append("secret_key", secret)
        }){
            url(tokenEndpoint)
            accept(jsonContentType)
            contentType(jsonContentType)
        }.also {
            println(
                it.bodyAsText()
            )
        }.body()
}