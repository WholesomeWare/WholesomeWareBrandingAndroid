package com.csakitheone.wholesomeware.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun Menu(
    modifier: Modifier = Modifier,
    children: @Composable MenuScope.() -> Unit,
) {
    val menuScope = remember { MenuScope() }

    Column(
        modifier = modifier,
    ) {
        children(menuScope)
    }
}

class MenuScope {
    data class ItemInfo(
        val enabled: Boolean = true,
        val onClick: () -> Unit = {},
        val text: String,
        val leadingIcon: @Composable (() -> Unit)? = null,
    )

    @Composable
    fun title(
        text: String,
    ) {
        Text(
            modifier = Modifier.padding(16.dp),
            text = text,
            style = MaterialTheme.typography.titleMedium,
        )
    }

    @Composable
    fun items(
        vararg items: ItemInfo,
    ) {
        items(items.toList())
    }

    @Composable
    fun items(
        items: List<ItemInfo>,
    ) {
        for (i in items.indices) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                onClick = items[i].onClick,
                enabled = items[i].enabled,
                shape = RoundedCornerShape(
                    topStart = if (i == 0) MaterialTheme.shapes.large.topStart else CornerSize(0),
                    topEnd = if (i == 0) MaterialTheme.shapes.large.topEnd else CornerSize(0),
                    bottomStart = if (i == items.size - 1) MaterialTheme.shapes.large.bottomStart else CornerSize(
                        0
                    ),
                    bottomEnd = if (i == items.size - 1) MaterialTheme.shapes.large.bottomEnd else CornerSize(
                        0
                    ),
                ),
            ) {
                Row(
                    modifier = Modifier.padding(24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    items[i].leadingIcon?.invoke()
                    Text(text = items[i].text)
                }
            }
        }
    }
}