package views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import util.currentDateTime


@Composable
fun <T> DropdownMenu(modifier: Modifier = Modifier, items: List<T>, default: T, onSelect: (T, Int) -> Unit, buildItem: @Composable (T) -> Unit) {
    var isExpanded by remember { mutableStateOf(false) }
    val duration = 500
    var selected by remember { mutableStateOf(default) }

    val enterExpand = remember {
        expandVertically(animationSpec = tween(duration))
    }
    val exitShrink = remember {
        shrinkVertically(animationSpec = tween(duration))
    }
    Box(modifier.wrapContentSize(Alignment.TopEnd)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.clickable { isExpanded = !isExpanded }) {
                buildItem(selected)
            }

            AnimatedVisibility(isExpanded, enter = enterExpand, exit = exitShrink) {
                LazyColumn {
                    items(items) {
                        if (it != selected) Box(Modifier.clickable {
                            selected = it
                            onSelect(it, currentDateTime().year)
                        }) {
                            buildItem(it)
                        }
                    }
                }
            }
        }
    }
}