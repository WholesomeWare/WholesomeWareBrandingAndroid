package com.csakitheone.wholesomeware.experiment

import android.app.SearchManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.provider.MediaStore
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.Scaffold
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.csakitheone.wholesomeware.R
import com.csakitheone.wholesomeware.service.RadioService
import com.csakitheone.wholesomeware.ui.components.Menu
import com.csakitheone.wholesomeware.ui.components.MenuScope
import com.csakitheone.wholesomeware_brand.ui.theme.WholesomewareBrandTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.URL

class RadioExperimentActivity : ComponentActivity() {
    private var radioService: RadioService? = null
    private var isServiceBound = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as RadioService.RadioBinder
            radioService = binder.getService()
            isServiceBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            radioService = null
            isServiceBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RadioExperimentScreen()
        }

        val intent = Intent(this, RadioService::class.java)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isServiceBound) {
            unbindService(serviceConnection)
            isServiceBound = false
        }
    }

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    @Preview
    fun RadioExperimentScreen() {
        WholesomewareBrandTheme {
            val coroutineScope = rememberCoroutineScope()

            var isPlaying by remember { mutableStateOf(false) }
            var radioNowPlaying by remember { mutableStateOf<String?>(null) }

            LaunchedEffect(Unit) {
                NetworkUtils.disableSSLCertificateVerify()
            }

            LaunchedEffect(radioService) {
                isPlaying = radioService?.isPlaying() == true
            }

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
                        Log.e("RadioExperimentActivity", "Error fetching radio metadata", e)
                    }
                }
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
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        ElevatedCard {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .widthIn(max = 400.dp)
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
                                                this@RadioExperimentActivity,
                                                RadioService::class.java
                                            ).apply {
                                                action = RadioService.ACTION_PLAY
                                                putExtra(RadioService.EXTRA_STREAM_URL, streamUrl)
                                                putExtra(
                                                    RadioService.EXTRA_STREAM_TITLE,
                                                    "Vörösmarty Rádió"
                                                )
                                            }
                                        startService(intent)
                                        isPlaying = true
                                        refreshRadioMetadata()
                                    } else {
                                        val intent =
                                            Intent(
                                                this@RadioExperimentActivity,
                                                RadioService::class.java
                                            ).apply {
                                                action = RadioService.ACTION_PAUSE
                                            }
                                        startService(intent)
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

                                        startActivity(
                                            Intent.createChooser(
                                                Intent(MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH)
                                                    .putExtra(
                                                        MediaStore.EXTRA_MEDIA_FOCUS,
                                                        "vnd.android.cursor.item/audio"
                                                    )
                                                    .putExtra(SearchManager.QUERY, "$artist $title"),
                                                //.putExtra(MediaStore.EXTRA_MEDIA_ARTIST, artist)
                                                //.putExtra(MediaStore.EXTRA_MEDIA_TITLE, title),
                                                "Lejátszás ezzel..."
                                            )
                                        )
                                    },
                                    title = "Keresés és lejátszás Intent küldése",
                                    description = MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH,
                                ),
                                MenuScope.ItemInfo(
                                    onClick = {
                                        startActivity(
                                            Intent(
                                                Intent.ACTION_VIEW,
                                                "https://cloudfront41.lexanetwork.com:7604".toUri()
                                            )
                                        )
                                    },
                                    title = "Rádió metaadatok forrása",
                                    description = "https://cloudfront41.lexanetwork.com:7604",
                                ),
                            )
                        }
                        Spacer(modifier = Modifier.navigationBarsPadding())
                    }
                }
            }
        }
    }
}
