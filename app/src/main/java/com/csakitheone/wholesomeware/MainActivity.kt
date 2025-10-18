package com.csakitheone.wholesomeware

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.csakitheone.wholesomeware.wallpaper.KoloraFesztAnalogClockWallpaperService
import com.csakitheone.wholesomeware_brand.WholesomeWare
import com.csakitheone.wholesomeware_brand.ui.components.WholesomeWareStoreButton
import com.csakitheone.wholesomeware_brand.ui.components.WholesomeWareStoreDropdownMenuItem
import com.csakitheone.wholesomeware_brand.ui.theme.WholesomewareBrandTheme
import java.util.Timer
import kotlin.concurrent.timerTask

class MainActivity : ComponentActivity() {
    private var isKeepingSplash = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen().setKeepOnScreenCondition { isKeepingSplash }
        enableEdgeToEdge()
        setContent {
            MainScreen()
        }
        Timer().schedule(timerTask {
            isKeepingSplash = false
        }, 3000L)
    }

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
    @Preview(device = "spec:parent=pixel_5,orientation=landscape")
    @Composable
    fun MainScreen() {
        WholesomewareBrandTheme {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background,
            ) {
                Column {
                    Surface(
                        shadowElevation = 8.dp,
                    ) {
                        Image(
                            modifier = Modifier.fillMaxWidth(),
                            painter = painterResource(id = R.drawable.wholesomeware_banner),
                            contentDescription = null,
                        )
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = "An app for everything WholesomeWare related.",
                        )
                        HorizontalDivider()
                        Text(
                            text = "Live wallpapers",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Card {
                            DropdownMenuItem(
                                leadingIcon = {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_access_time),
                                        contentDescription = null,
                                    )
                                },
                                text = { Text("Kolora Feszt analog clock") },
                                onClick = {
                                    try {
                                        val intent =
                                            Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER)
                                        intent.putExtra(
                                            WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                                            ComponentName(
                                                this@MainActivity,
                                                KoloraFesztAnalogClockWallpaperService::class.java
                                            )
                                        )
                                        startActivity(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(
                                            this@MainActivity,
                                            "Error setting wallpaper: ${e.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            )
                        }
                        HorizontalDivider()
                        Text(
                            text = "Widgets",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(text = "Coming soon...")
                    }
                }
            }
        }
    }
}
