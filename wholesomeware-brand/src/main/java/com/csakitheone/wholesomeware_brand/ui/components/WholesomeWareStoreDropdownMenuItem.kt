package com.csakitheone.wholesomeware_brand.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.csakitheone.wholesomeware_brand.R
import com.csakitheone.wholesomeware_brand.WholesomeWare

@Composable
fun WholesomeWareStoreDropdownMenuItem(
    modifier: Modifier = Modifier,
    text: String = "More apps",
    onClicked: () -> Unit = {},
) {
    val context = LocalContext.current

    DropdownMenuItem(
        modifier = modifier,
        onClick = {
            WholesomeWare.openPlayStore(context)
            onClicked()
        },
        leadingIcon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_wholesomeware),
                contentDescription = null
            )
        },
        text = { Text(text = text) },
    )
}