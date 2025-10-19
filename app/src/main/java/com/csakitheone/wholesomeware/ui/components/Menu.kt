package com.csakitheone.wholesomeware.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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

class WWMenuDefaults {
    companion object {

        val itemsSpacing = 2.dp

        @Composable
        fun itemsSpacer() =  Spacer(modifier = Modifier.padding(itemsSpacing))

        @Composable
        fun sectionSpacer() =  Spacer(modifier = Modifier.padding(8.dp))

        @Composable
        fun cardFirstItemShape() = RoundedCornerShape(
            topStart = MaterialTheme.shapes.large.topStart,
            topEnd = MaterialTheme.shapes.large.topEnd,
            bottomStart = CornerSize(0),
            bottomEnd = CornerSize(0),
        )

        @Composable
        fun cardMiddleItemShape() = RoundedCornerShape(0.dp)

        @Composable
        fun cardLastItemShape() = RoundedCornerShape(
            topStart = CornerSize(0),
            topEnd = CornerSize(0),
            bottomStart = MaterialTheme.shapes.large.bottomStart,
            bottomEnd = MaterialTheme.shapes.large.bottomEnd,
        )

        @Composable
        fun cardSingleItemShape() = MaterialTheme.shapes.large

    }
}

class MenuScope {
    data class ItemInfo(
        val enabled: Boolean = true,
        val onClick: () -> Unit = {},
        val title: String,
        val description: String? = null,
        val leadingIcon: @Composable (RowScope.() -> Unit)? = null,
        val trailingIcon: @Composable (RowScope.() -> Unit)? = null,
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
        Column(
            verticalArrangement = Arrangement.spacedBy(WWMenuDefaults.itemsSpacing),
        ) {
            for (i in items.indices) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = items[i].onClick,
                    enabled = items[i].enabled,
                    shape = when (i) {
                        0 -> if (items.size == 1) {
                            WWMenuDefaults.cardSingleItemShape()
                        } else {
                            WWMenuDefaults.cardFirstItemShape()
                        }
                        items.size - 1 -> WWMenuDefaults.cardLastItemShape()
                        else -> WWMenuDefaults.cardMiddleItemShape()
                    },
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        items[i].leadingIcon?.invoke(this)
                        Column {
                            Text(text = items[i].title)
                            items[i].description?.let { desc ->
                                Text(
                                    modifier = Modifier.alpha(.6f),
                                    text = desc,
                                    style = MaterialTheme.typography.labelSmall,
                                )
                            }
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        items[i].trailingIcon?.invoke(this)
                    }
                }
            }
        }
    }
}