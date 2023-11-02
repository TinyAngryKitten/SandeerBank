package views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import db.ProcessedTransaction
import db.TransactionCategory
import db.TransactionDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import org.koin.core.component.get
import views.style.body
import views.style.h2
import views.style.h4

@Composable
fun TransactionsScreen(transactions: List<ProcessedTransaction>) {
    LazyColumn {
        items(transactions) { ExpandableTransactionView(it) }
    }
}

@Composable
fun ExpandableTransactionView(
    transaction: ProcessedTransaction
) {
    var isExpanded by remember { mutableStateOf(false) }
    val expandDuration = 500
    val transitionState = remember { MutableTransitionState(isExpanded).apply { targetState = !isExpanded } }
    val transition = updateTransition(transitionState, label = "transition")
    val cardElevation by transition.animateDp({
        tween(durationMillis = expandDuration)
    }, label = "elevationTransition") {
        if (isExpanded) 24.dp else 5.dp
    }
    val cardRoundedCorners by transition.animateDp({
        tween(
            durationMillis = expandDuration,
            easing = FastOutSlowInEasing
        )
    }, label = "cornersTransition") {
        if (isExpanded) 16.dp else 0.dp
    }
    val enterFadeIn = remember {
        fadeIn(
            animationSpec = TweenSpec(
                durationMillis = expandDuration,
                easing = FastOutLinearInEasing
            )
        )
    }
    val enterExpand = remember {
        expandVertically(animationSpec = tween(expandDuration))
    }
    val exitFadeOut = remember {
        fadeOut(
            animationSpec = TweenSpec(
                durationMillis = expandDuration,
                easing = LinearOutSlowInEasing
            )
        )
    }
    val exitCollapse = remember {
        shrinkVertically(animationSpec = tween(expandDuration))
    }
    Card(modifier = Modifier.clickable { isExpanded = !isExpanded }, elevation = cardElevation, shape = MaterialTheme.shapes.medium.copy(CornerSize(cardRoundedCorners))) {
        AnimatedVisibility(isExpanded, enter = enterExpand + enterFadeIn, exit = exitCollapse + exitFadeOut) { ExpandedTransactionView(transaction) }
        AnimatedVisibility(!isExpanded, enter = enterFadeIn, exit = exitFadeOut) { CompactTransactionView(transaction) }
    }
}


@OptIn(ExperimentalResourceApi::class)
@Composable
fun CompactTransactionView(transaction: ProcessedTransaction) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(30.dp)
    ) {
        Icon(painterResource(transaction.category?.iconFile ?: "xml/Other.xml"), "", modifier = Modifier.size(50.dp).padding(end = 15.dp))

        Column(horizontalAlignment = Alignment.Start) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,) {
                Text(
                    transaction.processedMerchantName ?: transaction.rawMerchantName ?: "Ukjent",
                    style = h4
                )
                Text("${transaction.amount} ${transaction.currency}", style = h4)
            }

            Text(transaction.bookingTimestamp ?: "", style = body, color = Color.LightGray)
        }
    }
}