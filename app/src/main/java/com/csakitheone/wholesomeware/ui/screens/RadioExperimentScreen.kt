package com.csakitheone.wholesomeware.ui.screens

import android.app.SearchManager
import android.content.Intent
import android.provider.MediaStore
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.material3.ToggleFloatingActionButtonDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.csakitheone.wholesomeware.R
import com.csakitheone.wholesomeware.experiment.NetworkUtils
import com.csakitheone.wholesomeware.service.RadioService
import com.csakitheone.wholesomeware.ui.components.Menu
import com.csakitheone.wholesomeware.ui.components.MenuScope
import com.csakitheone.wholesomeware.ui.navigation.Navigator
import com.csakitheone.wholesomeware_brand.ui.theme.WholesomewareBrandTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.URL

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun RadioExperimentScreen(
    navigator: Navigator,
    radioService: RadioService?
) {
    WholesomewareBrandTheme {
        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()

        var isPlaying by remember { mutableStateOf(false) }
        var radioNowPlaying by rememberSaveable { mutableStateOf<String?>(null) }

        fun refreshRadioMetadata() {
            radioNowPlaying = "Betöltés..."
            val url = "https://cloudfront41.lexanetwork.com:7604"
            coroutineScope.launch(Dispatchers.IO) {
                try {
                    val text =
                        String(
                            URL(url).readText().toByteArray(Charsets.ISO_8859_1),
                            Charsets.UTF_8
                        )
                    radioNowPlaying = text
                        .substringAfter("Current Song:")
                        .substringAfter("streamdata\">")
                        .substringBefore("<").trim()
                } catch (e: Exception) {
                    Log.e("RadioExperimentScreen", "Error fetching radio metadata", e)
                }
            }
        }

        LaunchedEffect(Unit) {
            NetworkUtils.disableSSLCertificateVerify()
            refreshRadioMetadata()
        }

        LaunchedEffect(radioService) {
            isPlaying = radioService?.isPlaying() == true
        }

        Surface(
            color = MaterialTheme.colorScheme.background,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
            ) {
                TopAppBar(
                    title = { Text(text = "Kísérlet: Rádió") },
                    navigationIcon = {
                        IconButton(
                            onClick = { navigator.goBack() },
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_arrow_back),
                                contentDescription = "Vissza",
                            )
                        }
                    },
                )
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    ElevatedCard(
                        modifier = Modifier.widthIn(max = 320.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "Vörösmarty Rádió",
                                style = MaterialTheme.typography.titleLarge,
                            )
                        }
                    }
                    Text(
                        text = radioNowPlaying ?: "A rádió műsora még nem lett lekérve.",
                    )
                    Button(
                        onClick = {
                            refreshRadioMetadata()
                        }
                    ) {
                        Icon(
                            modifier = Modifier.padding(end = ButtonDefaults.IconSpacing),
                            painter = painterResource(R.drawable.ic_refresh),
                            contentDescription = "Frissítés"
                        )
                        Text(text = "Műsor frissítése")
                    }
                    Box(
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularWavyProgressIndicator(
                            modifier = Modifier
                                .size(80.dp)
                                .alpha(if (isPlaying) 1f else 0f),
                        )
                        ToggleFloatingActionButton(
                            checked = isPlaying,
                            onCheckedChange = { isChecked ->
                                if (isChecked) {
                                    val streamUrl =
                                        "https://cloudfront41.lexanetwork.com:7604/livestream.mp3"
                                    val intent =
                                        Intent(
                                            context,
                                            RadioService::class.java
                                        ).apply {
                                            action = RadioService.ACTION_PLAY
                                            putExtra(RadioService.EXTRA_STREAM_URL, streamUrl)
                                            putExtra(
                                                RadioService.EXTRA_STREAM_TITLE,
                                                "Vörösmarty Rádió"
                                            )
                                        }
                                    context.startService(intent)
                                    isPlaying = true
                                    refreshRadioMetadata()
                                } else {
                                    val intent =
                                        Intent(
                                            context,
                                            RadioService::class.java
                                        ).apply {
                                            action = RadioService.ACTION_PAUSE
                                        }
                                    context.startService(intent)
                                    isPlaying = false
                                }
                            },
                        ) {
                            Icon(
                                painter = painterResource(
                                    if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play_arrow
                                ),
                                contentDescription = null,
                                tint = ToggleFloatingActionButtonDefaults.iconColor()(if (isPlaying) 1f else 0f),
                            )
                        }
                    }
                    Menu {
                        items(
                            MenuScope.ItemInfo(
                                enabled = radioNowPlaying != null && radioNowPlaying!!.contains(" - "),
                                onClick = {
                                    val artist = radioNowPlaying!!.substringBefore(" - ").trim()
                                    val title = radioNowPlaying!!.substringAfter(" - ").trim()

                                    context.startActivity(
                                        Intent.createChooser(
                                            Intent(MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH)
                                                .putExtra(
                                                    MediaStore.EXTRA_MEDIA_FOCUS,
                                                    "vnd.android.cursor.item/audio"
                                                )
                                                .putExtra(SearchManager.QUERY, "$artist $title"),
                                            "Lejátszás ezzel..."
                                        )
                                    )
                                },
                                title = "Keresés és lejátszás Intent küldése",
                                description = MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH,
                            ),
                            MenuScope.ItemInfo(
                                onClick = {
                                    context.startActivity(
                                        Intent(
                                            Intent.ACTION_VIEW,
                                            "https://cloudfront41.lexanetwork.com:7604".toUri()
                                        )
                                    )
                                },
                                title = "Rádió metaadatok forrása",
                                description = "https://cloudfront41.lexanetwork.com:7604",
                                trailingIcon = {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_open_in_new),
                                        contentDescription = null
                                    )
                                },
                            ),
                        )
                    }
                    Spacer(modifier = Modifier.navigationBarsPadding())
                }
            }
        }
    }
}
