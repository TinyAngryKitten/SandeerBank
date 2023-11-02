package views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.estimateAnimationDurationMillis
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import db.ProcessedTransaction
import db.TransactionCategory
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import views.style.h4

@OptIn(ExperimentalResourceApi::class)
@Composable
fun OverviewScreen(transactions: List<ProcessedTransaction>) {
    LaunchedEffect(transactions) {
        println("TOTAL TRANSACTIONS: ${
            transactions.fold(0f) { acc, t -> if(t.amount < 0) acc - t.amount else acc 
        }}")
    }
    var spendingPerCategory by remember { mutableStateOf(TransactionCategory.values().associateWith { Pair(0f, listOf<ProcessedTransaction>()) }) }

    LaunchedEffect(transactions.size) {
        transactions.sortedByDescending { it.bookingDate }
        spendingPerCategory = TransactionCategory.values().associateWith { category ->
            transactions.fold(Pair(0f, listOf())) { acc, transaction ->
                if(transaction.isOfCategory(category)) {
                    Pair(acc.first + (transaction.processedAmount ?: transaction.amount), acc.second + transaction)
                } else acc
            }
        }
    }

    LazyColumn{
        items(TransactionCategory.values()) {
            val (totalSpending, transactionList) = spendingPerCategory[it]!!
            UsagePerCategoryView(it, totalSpending, transactionList)
        }
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
private fun UsagePerCategoryView(category: TransactionCategory, totalSpending: Float, transactions: List<ProcessedTransaction>) {
    val duration = 500
    var isExpanded by remember { mutableStateOf(false) }
    val transactionState = remember { MutableTransitionState(false).apply { targetState = true } }
    val transition = updateTransition(transactionState)

    val cardElevation by transition.animateDp({
        tween(durationMillis = duration)
    }, label = "elevationTransition") {
        if (isExpanded) 24.dp else 5.dp
    }

    val enterExpand = remember {
        expandVertically(animationSpec = tween(duration))
    }
    val exitShrink = remember {
        shrinkVertically(animationSpec = tween(duration))
    }

    Column {
        Card(elevation = cardElevation) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(30.dp).clickable { isExpanded = !isExpanded }
            ) {
                Icon(
                    painterResource(category.iconFile),
                    "",
                    modifier = Modifier.size(50.dp).padding(end = 15.dp)
                )

                Column(horizontalAlignment = Alignment.Start) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            category.name,
                            style = h4
                        )
                        Text("$totalSpending NOK", style = h4)
                    }
                }
            }
        }

        AnimatedVisibility(isExpanded, enter = enterExpand, exit = exitShrink) {
            Column {
                transactions.forEach { CompactTransactionView(it) }
            }
        }
    }
}

private fun startDateForMonth(date: LocalDate? = null) {
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    if(now.year == date?.year && now.month == date.month && now.dayOfMonth <= 15) LocalDate(year = now.year, monthNumber = lastMonth(date.monthNumber), dayOfMonth = 15)
    if(now.dayOfMonth >= 15) date
}

private fun lastMonth(month: Int) =
    if(month == Month.JANUARY.ordinal) Month.DECEMBER.ordinal else month-1