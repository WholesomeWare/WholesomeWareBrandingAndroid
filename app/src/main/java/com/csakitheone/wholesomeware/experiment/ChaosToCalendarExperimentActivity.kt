package com.csakitheone.wholesomeware.experiment

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.csakitheone.wholesomeware.R
import com.csakitheone.wholesomeware_brand.ui.theme.WholesomewareBrandTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class ChaosToCalendarExperimentActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChaosToCalendarExperimentScreen()
        }
    }

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
    @Preview
    @Composable
    fun ChaosToCalendarExperimentScreen() {
        WholesomewareBrandTheme {
            val coroutineScope = rememberCoroutineScope()

            var rawInput by remember { mutableStateOf("") }
            var aiReply by remember { mutableStateOf("") }
            var isAiThinking by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                launch(Dispatchers.IO) {
                    OllamaClient.findWorkingHost()
                }
            }

            Surface(
                color = MaterialTheme.colorScheme.background,
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    TopAppBar(
                        title = { Text(text = "Kísérlet: Káoszból naptárba") },
                        navigationIcon = {
                            IconButton(
                                onClick = { finish() },
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_arrow_back),
                                    contentDescription = "Vissza",
                                )
                            }
                        },
                    )
                    Column(
                        modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
                    ) {
                        AnimatedVisibility(isAiThinking) {
                            LoadingIndicator()
                        }
                        AnimatedContent(targetState = aiReply) {
                            Text(
                                modifier = Modifier.padding(16.dp),
                                text = it.ifBlank { "Ide fogja írni a mesterséges intelligencia, hogy mit értett meg a káoszból, amit beírtál alul. Ez egy kísérlet, hogy mennyire tudja értelmezni a szabad szöveget és milyen naptári eseményeket tud belőle létrehozni." },
                            )
                        }
                    }
                    Surface(
                        color = BottomSheetDefaults.ContainerColor,
                        shape = BottomSheetDefaults.ExpandedShape,
                    ) {
                        Column(
                            modifier = Modifier
                                .navigationBarsPadding()
                                .imePadding()
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            OutlinedTextField(
                                enabled = !isAiThinking,
                                modifier = Modifier.fillMaxWidth(),
                                value = rawInput,
                                onValueChange = { rawInput = it },
                                label = { Text("Káosz: Írj vagy diktálj ide bármit") },
                                minLines = 3,
                                maxLines = 10,
                            )
                            Button(
                                enabled = rawInput.isNotBlank() && !isAiThinking,
                                modifier = Modifier.fillMaxWidth(),
                                onClick = {
                                    isAiThinking = true
                                    coroutineScope.launch(Dispatchers.IO) {
                                        aiReply = OllamaClient.generate(
                                            model = "qwen3.5:4b",
                                            prompt = rawInput,
                                            system = """
                                                Te egy adatfeldolgozó API vagy. A feladatod, hogy a bejövő strukturálatlan szövegből naptári eseményeket nyerj ki, és KIZÁRÓLAG érvényes JSON formátumban válaszolj.
                                                A jelenlegi pontos dátum és idő: ${LocalDateTime.now()}

                                                Szabályok:
                                                1. A válaszod nem tartalmazhat semmi mást, csak a JSON tömböt. Nincs bevezetés, nincs magyarázat.
                                                2. Ha relatív időpontot látsz (pl. holnap), használd a jelenlegi dátumot a kiszámításához.
                                                3. A dátumokat ISO 8601 formátumban add meg (YYYY-MM-DDTHH:mm:ss). Ha az időpont nem ismert, legyen null.

                                                Kötelező JSON struktúra:
                                                [
                                                  {
                                                    "title": "Esemény neve",
                                                    "startTime": "2026-03-06T15:00:00",
                                                    "endTime": null,
                                                    "isAllDay": false,
                                                    "location": null,
                                                    "description": null
                                                  }
                                                ]
                                            """.trimIndent()
                                        )
                                        isAiThinking = false
                                    }
                                },
                            ) {
                                Text(text = "Ezt bogozd ki!")
                            }
                        }
                    }
                }
            }
        }
    }
}
