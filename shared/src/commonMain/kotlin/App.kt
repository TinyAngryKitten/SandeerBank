import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import chatgpt.ChatGptClient
import db.ProcessedTransaction
import db.TransactionDatabase
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimePeriod
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.minus
import nordigen.NordigenClient
import nordigen.accountId
import org.koin.core.component.get
import util.currentDateTime
import views.BottomBar
import views.BottomBarItem
import views.Destination
import views.Destination.*
import views.NewRequisitionScreen
import views.OverviewScreen
import views.TopBar
import views.TransactionsScreen

private val bottomBarItems = listOf(
    BottomBarItem("xml/Electronics.xml", "Transaction", Transactions),
    BottomBarItem("xml/Other.xml", "Overview", Overview),
    BottomBarItem("xml/Plus-circle.xml", "Add bank", AddRequisition)
)

private fun daysInMonth(year: Int, month: Month) =
    if(month == Month.DECEMBER) LocalDate(year, Month.JANUARY, 1)
        .minus(1, DateTimeUnit.DAY)
        .dayOfMonth
    else LocalDate(year, month.ordinal+2/*ordinal starts at 0, arg counts from 1*/, 1)
        .minus(1, DateTimeUnit.DAY)
        .dayOfMonth
private fun createAfterAndBeforeDate(month: Month, year: Int = currentDateTime().year) =
    Pair(LocalDate(year, month, 1), LocalDate(year, month, daysInMonth(year, month)))

@Composable
fun App() {
    MaterialTheme {
        var currentDestination by remember { mutableStateOf<Destination>(Transactions) }
        var dateConstraint by remember { mutableStateOf(createAfterAndBeforeDate(currentDateTime().month)) }

        Scaffold(
            bottomBar = { BottomBar(bottomBarItems, currentDestination, navigate = { currentDestination = it }) },
            topBar = { TopBar { month, year -> dateConstraint = createAfterAndBeforeDate(month, year) } }
        ) {
            var allTransactions by remember { mutableStateOf<List<ProcessedTransaction>>(listOf()) }
            var transactions by remember { mutableStateOf(listOf<ProcessedTransaction>()) }

            LaunchedEffect(Unit) {
                KoinHelper.get<TransactionDatabase>().fetchTransactions().asFlow().collect {
                    allTransactions = it.list.sortedByDescending { it.bookingDate!! }
                }
            }
            LaunchedEffect(allTransactions, dateConstraint) {
                val (after, before) = dateConstraint
                transactions = allTransactions.filter { it.bookingDate!! in after..before }
            }

            Box(modifier = Modifier.padding(bottom = it.calculateBottomPadding())) {
                when (currentDestination) {
                    is Transactions -> TransactionsScreen(transactions)
                    is Overview -> OverviewScreen(transactions)
                    is AddRequisition -> NewRequisitionScreen(KoinHelper.get())
                }
            }
        }
    }
}