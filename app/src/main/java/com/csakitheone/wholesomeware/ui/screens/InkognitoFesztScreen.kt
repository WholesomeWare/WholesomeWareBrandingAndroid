package com.csakitheone.wholesomeware.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.csakitheone.wholesomeware.R
import com.csakitheone.wholesomeware.data.InkognitoFesztRepository
import com.csakitheone.wholesomeware.ui.navigation.Navigator
import com.csakitheone.wholesomeware.ui.theme.InkognitoTheme
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InkognitoFesztScreen(
    navigator: Navigator
) {
    val context = LocalContext.current
    var selectedDay by remember {
        val dayOfMonth = LocalDate.now().dayOfMonth
        mutableIntStateOf(
            if ((17..19).contains(dayOfMonth)) dayOfMonth else 17
        )
    }
    var selectedStage by remember { mutableStateOf("") }
    val events = remember(selectedDay, selectedStage) {
        InkognitoFesztRepository.programs
            .filter { it.startTime.dayOfMonth == selectedDay }
            .filter { selectedStage.isBlank() || it.stage == selectedStage }
    }
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    var now by remember { mutableStateOf(LocalDateTime.now()) }

    LaunchedEffect(Unit) {
        while (true) {
            now = LocalDateTime.now()
            delay(10.seconds)
        }
    }

    InkognitoTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "INKognito Fesztivál 2026",
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navigator.goBack() }) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_arrow_back),
                                contentDescription = "Vissza"
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            context.startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    InkognitoFesztRepository.facebookEventUrl.toUri()
                                )
                            )
                        }) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_facebook),
                                contentDescription = "Facebook esemény"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.primary,
                        navigationIconContentColor = MaterialTheme.colorScheme.primary,
                        actionIconContentColor = MaterialTheme.colorScheme.primary,
                    )
                )
            },
            bottomBar = {
                NavigationBar {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        (17..19).forEach { day ->
                            NavigationBarItem(
                                selected = selectedDay == day,
                                onClick = { selectedDay = day },
                                icon = {
                                    Text(
                                        text = "$day",
                                        style = MaterialTheme.typography.titleLarge
                                    )
                                },
                                label = {
                                    Text(
                                        when (day) {
                                            17 -> "Péntek"
                                            18 -> "Szombat"
                                            19 -> "Vasárnap"
                                            else -> ""
                                        }
                                    )
                                }
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                color = MaterialTheme.colorScheme.background
            ) {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Row {
                            FilterChip(
                                selected = selectedStage == "Belső színpad",
                                onClick = {
                                    selectedStage =
                                        if (selectedStage == "Belső színpad") "" else "Belső színpad"
                                },
                                label = { Text("Belső színpad") },
                            )
                            FilterChip(
                                selected = selectedStage == "Külső színpad",
                                onClick = {
                                    selectedStage =
                                        if (selectedStage == "Külső színpad") "" else "Külső színpad"
                                },
                                label = { Text("Külső színpad") },
                            )
                        }
                    }

                    items(events, { it.title }) { event ->
                        val progress by remember(now, event.startTime, event.endTime) {
                            derivedStateOf {
                                if (event.endTime == null) return@derivedStateOf 0f
                                val nowSeconds = now.toEpochSecond(ZoneOffset.UTC)
                                val start = event.startTime.toEpochSecond(ZoneOffset.UTC)
                                val end = event.endTime.toEpochSecond(ZoneOffset.UTC)
                                if (nowSeconds < start) return@derivedStateOf 0f
                                val progress =
                                    (nowSeconds - start) / (end - start).toFloat()
                                progress.coerceIn(0f, 1f)
                            }
                        }

                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            ListItem(
                                supportingContent = {
                                    val timeRange = if (event.endTime != null) {
                                        "${event.startTime.format(timeFormatter)} – ${
                                            event.endTime.format(
                                                timeFormatter
                                            )
                                        }"
                                    } else {
                                        event.startTime.format(timeFormatter)
                                    }
                                    Text(
                                        text = "$timeRange | ${event.stage}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                },
                                leadingContent = {
                                    Text(
                                        text = event.emoji,
                                        style = MaterialTheme.typography.headlineSmall
                                    )
                                },
                                colors = ListItemDefaults.colors(
                                    containerColor = Color.Transparent,
                                ),
                            ) {
                                Text(
                                    text = event.title,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                            if (progress > 0f) {
                                LinearProgressIndicator(
                                    modifier = Modifier.fillMaxWidth(),
                                    progress = { progress },
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.secondary,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
