package views

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import nordigen.NordigenClient

@Composable
fun NewRequisitionScreen(nordigenClient: NordigenClient) {
    Column {
        val defaultBank = "MORROW_NDEANOKK"
        var link: String? by remember { mutableStateOf(null)}
        var bank: String by remember { mutableStateOf(defaultBank)}
        TextField(defaultBank, onValueChange = { bank = it })
        Button(onClick = {
            CoroutineScope(Dispatchers.Unconfined).launch {
                val response = nordigenClient.fetchNewRequisitionLink(bank)
                link = response.link
            }
        }) {
            Text("Generate link")
        }

        SelectionContainer {
            Text(link ?: "",)
        }
    }
}