import chatgpt.ChatGptClient
import com.russhwolf.settings.Settings
import currencyapi.CurrencyApi
import currencyapi.CurrencyConverter
import db.TransactionDatabase
import generated.BuildConfig
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import nordigen.NordigenClient
import nordigen.accountId
import org.koin.core.component.KoinComponent
import org.koin.dsl.module
import org.koin.core.component.get
import search.BingSearch
import search.SearchApi

class KoinHelper: KoinComponent {
    companion object: KoinComponent
    //Startuplogic
    fun startKoin() {
        org.koin.core.context.startKoin {
            modules(koinModule)
            CoroutineScope(Dispatchers.Default).launch {
                Startup().fetchAndProcessTransactions(accountId, "Komplett")
                println("OWOWOWOOWOWOWOWOWOOOOOO: " + Json.encodeToString(get<SearchApi>().findPicture("Meny")?.value?.first()))
            }
        }
    }

    private val koinModule = module {
        single { Settings() }
        single<SearchApi> { BingSearch(BuildConfig.bingSecret) }
        single {
            NordigenClient(
                secret = BuildConfig.nordigenSecret,
                secretId = BuildConfig.nordigenClientId,
                endpoint = "https://ob.nordigen.com/api/v2/"
            )
        }
        single { TransactionDatabase() }
        single { ChatGptClient(BuildConfig.chatGptSecret) }
        single<CurrencyConverter> { CurrencyApi(BuildConfig.currencyApiSecret) }
        single {
            HttpClient {
                install(HttpTimeout) {
                    socketTimeoutMillis = 15000
                    requestTimeoutMillis = 15000
                    connectTimeoutMillis = 15000
                }
                install(ContentNegotiation) {
                    json(Json {
                        prettyPrint = true
                        isLenient = true
                        ignoreUnknownKeys = true
                    })
                }
            }
        }
    }
}