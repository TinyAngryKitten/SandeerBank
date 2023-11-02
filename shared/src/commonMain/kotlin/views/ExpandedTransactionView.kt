package views

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
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

@OptIn(ExperimentalResourceApi::class, ExperimentalFoundationApi::class)
@Composable
fun ExpandedTransactionView(transaction: ProcessedTransaction) {
    val client = KoinHelper.get<TransactionDatabase>()
    var editable: Boolean by remember { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = { editable = !editable }
                )
            }
    ) {
        Text("${transaction.amount} ${transaction.currency}", style = h2, modifier = Modifier.padding(start = 20.dp, top = 20.dp,end = 20.dp, bottom = 10.dp))
        Text("Booked: ${transaction.bookingTimestamp}", style = body, color = Color.LightGray)
        Text("Value: ${transaction.valueTimestamp}", style = body, color = Color.LightGray)
        Text("Raw name: ${transaction.rawMerchantName}", style = body, color = Color.LightGray)

        EditableMerchant(transaction, client, editable)

        EditableTransactionCategory(transaction, client)
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
private fun EditableMerchant(transaction: ProcessedTransaction, client: TransactionDatabase, editable: Boolean) {
    var text by remember { mutableStateOf(transaction.processedMerchantName ?: transaction.rawMerchantName) }

    if(!editable) Text("Merchant: ${transaction.processedMerchantName ?: transaction.rawMerchantName}")
    else {
        TextField(
            value = text ?: "",
            onValueChange = { text = it },
        )

        Button(onClick = {
            CoroutineScope(Dispatchers.Default).launch {
                client.setProcessedMerchantName(transaction.rawMerchantName!!, text!!)
            }
        }) {
            Text("Update")
        }
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
private fun EditableTransactionCategory(transaction: ProcessedTransaction, client: TransactionDatabase) {
    Column {
        Row {
            Text("Category: ${transaction.category?.name}")
            transaction.category?.iconFile?.let {
                Icon(
                    painterResource(it),
                    "",
                    modifier = Modifier.size(30.dp).padding(15.dp)
                )
            }
        }
        //Change category
        Row {
            TransactionCategory.values().forEach {
                Icon(
                    painterResource(it.iconFile),
                    "",
                    modifier = Modifier.size(10.dp)
                        .padding(15.dp)
                        .clickable {
                            CoroutineScope(Dispatchers.Default).launch {
                                client.updateCategoryForAll(transaction.rawMerchantName ?: return@launch , it)
                            }
                        }
                )
            }
        }
    }
}