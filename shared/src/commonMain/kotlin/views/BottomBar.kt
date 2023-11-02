package views

import androidx.compose.foundation.layout.size
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource

sealed class Destination {
    object Transactions: Destination()
    object Overview: Destination()
    object AddRequisition: Destination()
}

data class BottomBarItem(
    val icon: String,
    val label: String,
    val destination: Destination,
)

@OptIn(ExperimentalResourceApi::class)
@Composable
fun BottomBar(items: List<BottomBarItem>, current: Destination, navigate: (Destination) -> Unit) {
    BottomNavigation {
        items.forEach { screen ->
            BottomNavigationItem(
                icon = { Icon(painterResource(screen.icon), "", modifier = Modifier.size(25.dp)) },
                selected = current == screen.destination,
                label = { Text(screen.label) },
                onClick = {
                    if(current != screen.destination) navigate(screen.destination)
                }
            )
        }
    }
}