package com.csakitheone.wholesomeware.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun Menu(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    children: @Composable MenuScope.() -> Unit,
) {
    val menuScope = remember { MenuScope() }

    Column(
        modifier = modifier,
    ) {
        Column(modifier = Modifier.padding(contentPadding)) {
            children(menuScope)
        }
    }
}

class WWMenuDefaults {
    companion object {

        val itemsSpacing = 2.dp

        @Composable
        fun itemsSpacer() = Spacer(modifier = Modifier.padding(itemsSpacing))

        @Composable
        fun sectionSpacer() = Spacer(modifier = Modifier.padding(8.dp))

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
        val shapeOverride: Shape? = null,
        val backgroundImage: Painter? = null,
    )

    @Composable
    fun title(
        text: String,
        subtitle: String? = null,
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
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
                    shape = items[i].shapeOverride ?: when (i) {
                        0 -> if (items.size == 1) {
                            WWMenuDefaults.cardSingleItemShape()
                        } else {
                            WWMenuDefaults.cardFirstItemShape()
                        }

                        items.size - 1 -> WWMenuDefaults.cardLastItemShape()
                        else -> WWMenuDefaults.cardMiddleItemShape()
                    },
                ) {
                    Box {
                        if (items[i].backgroundImage != null) {
                            Image(
                                modifier = Modifier
                                    .matchParentSize()
                                    .fillMaxWidth()
                                    .alpha(.2f),
                                painter = items[i].backgroundImage!!,
                                contentDescription = null,
                                contentScale = ContentScale.FillWidth,
                            )
                        }
                        Row(
                            modifier = Modifier.padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            items[i].leadingIcon?.invoke(this)
                            Column(
                                modifier = Modifier.weight(1f),
                            ) {
                                Text(text = items[i].title)
                                if (!items[i].description.isNullOrBlank()) {
                                    Text(
                                        modifier = Modifier.alpha(.6f),
                                        text = items[i].description ?: "",
                                        style = MaterialTheme.typography.labelSmall,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }
                            items[i].trailingIcon?.invoke(this)
                        }
                    }
                }
            }
        }
    }
}