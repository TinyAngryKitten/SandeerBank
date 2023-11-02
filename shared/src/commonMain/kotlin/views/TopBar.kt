package views

import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex
import kotlinx.datetime.Month
import util.currentDateTime

@Composable
fun TopBar(onDateChange: (Month, Int) -> Unit) {
    TopAppBar {
        DropdownMenu(modifier = Modifier.zIndex(30f), Month.values().asList(), currentDateTime().month, onDateChange) {
            Text(it.toString())
        }
    }
}