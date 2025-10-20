package com.csakitheone.wholesomeware

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.csakitheone.wholesomeware.ui.components.Menu
import com.csakitheone.wholesomeware.ui.components.MenuScope
import com.csakitheone.wholesomeware.wallpaper.KoloraFesztAnalogClockWallpaperService
import com.csakitheone.wholesomeware_brand.ui.theme.WholesomewareBrandTheme
import androidx.core.net.toUri
import androidx.glance.appwidget.GlanceAppWidgetManager
import com.csakitheone.wholesomeware.ui.components.WWMenuDefaults
import com.csakitheone.wholesomeware.wallpaper.TemplateWallpaperService
import com.csakitheone.wholesomeware.widget.KoloraFesztAnalogClockWidget
import com.csakitheone.wholesomeware.widget.KoloraFesztAnalogClockWidgetReceiver
import kotlinx.coroutines.launch
import kotlin.math.min

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        enableEdgeToEdge()
        setContent {
            MainScreen()
        }
    }

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    fun MainScreen() {
        WholesomewareBrandTheme {
            val density = LocalDensity.current
            val coroutineScope = rememberCoroutineScope()

            var smallHeaderAlpha by remember { mutableFloatStateOf(0f) }
            var headerMinHeight by remember { mutableFloatStateOf(0f) }
            val headerMaxHeight = remember {
                val screenWidth = resources.displayMetrics.widthPixels.toFloat()
                screenWidth * 2305 / 4097
            }
            var headerHeight by remember { mutableFloatStateOf(headerMaxHeight) }
            val menuScrollState = rememberScrollState()
            val nestedScrollConnection = remember(headerMinHeight) {
                object : NestedScrollConnection {
                    override fun onPreScroll(
                        available: Offset,
                        source: NestedScrollSource
                    ): Offset {
                        val delta = available.y

                        if (delta > 0 && menuScrollState.canScrollBackward) return Offset.Zero

                        val newHeaderHeight = headerHeight + delta
                        val prevHeaderHeight = headerHeight
                        headerHeight = newHeaderHeight.coerceIn(headerMinHeight, headerMaxHeight)
                        val consumed = headerHeight - prevHeaderHeight

                        if (headerMinHeight > 0f) {
                            smallHeaderAlpha =
                                1f - (headerHeight - headerMinHeight) / (headerMaxHeight - headerMinHeight)
                        }

                        return Offset(0f, consumed)
                    }
                }
            }

            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background,
            ) {
                Column(
                    modifier = Modifier.nestedScroll(nestedScrollConnection),
                ) {
                    Surface(
                        modifier = Modifier.height(with(density) { headerHeight.toDp() }),
                        shadowElevation = 8.dp,
                    ) {
                        Box(
                            contentAlignment = Alignment.TopCenter,
                        ) {
                            Image(
                                modifier = Modifier.fillMaxWidth(),
                                painter = painterResource(id = R.drawable.wholesomeware_banner),
                                contentDescription = null,
                                contentScale = ContentScale.FillWidth,
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .windowInsetsTopHeight(WindowInsets.statusBars)
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(
                                                MaterialTheme.colorScheme.primary,
                                                Color.Transparent
                                            ),
                                        )
                                    ),
                            )
                            TopAppBar(
                                modifier = Modifier
                                    .alpha(smallHeaderAlpha)
                                    .onGloballyPositioned {
                                        headerMinHeight = min(it.size.height.toFloat(), headerMaxHeight - 1f)
                                    },
                                title = { Text(text = "WholesomeWare") },
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                                ),
                            )
                        }
                    }
                    Menu(
                        modifier = Modifier
                            .verticalScroll(menuScrollState)
                            .fillMaxSize()
                            .padding(16.dp)
                            .navigationBarsPadding(),
                    ) {
                        ElevatedCard(shape = WWMenuDefaults.cardFirstItemShape()) {
                            Text(
                                modifier = Modifier.padding(16.dp),
                                text = "“You might not think that programmers are artists, but programming is an extremely creative profession. It’s logic-based creativity.”\n– John Romero",
                                style = MaterialTheme.typography.bodySmallEmphasized,
                                fontStyle = FontStyle.Italic,
                            )
                        }
                        WWMenuDefaults.itemsSpacer()
                        ElevatedCard(shape = WWMenuDefaults.cardLastItemShape()) {
                            Text(
                                modifier = Modifier.padding(16.dp),
                                text = "A WholesomeWare app olyan alkotások gyűjteménye, amelyeket könnyebb vagy csak mobil alkalmazásban lehet megjeleníteni. Élő hátterek, widget-ek és egyéb apróságok, amelyeket a barátaim, művész ismerősök vagy én készítettem.",
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                        WWMenuDefaults.sectionSpacer()
                        items(
                            MenuScope.ItemInfo(
                                onClick = {
                                    startActivity(
                                        Intent(
                                            Intent.ACTION_VIEW,
                                            "https://kolora.web.app/".toUri()
                                        )
                                    )
                                },
                                title = "Kolora Egyesület",
                                description = "A WholesomeWare nem hivatalos szülő szervezete",
                                leadingIcon = {
                                    Icon(
                                        modifier = Modifier.size(24.dp),
                                        painter = painterResource(id = R.drawable.ic_kolora),
                                        contentDescription = null,
                                    )
                                },
                            ),
                            MenuScope.ItemInfo(
                                onClick = {
                                    startActivity(
                                        Intent(
                                            Intent.ACTION_VIEW,
                                            "https://github.com/WholesomeWare".toUri()
                                        )
                                    )
                                },
                                title = "GitHub",
                                leadingIcon = {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_github),
                                        contentDescription = null,
                                    )
                                },
                                trailingIcon = {
                                    Button(
                                        onClick = {
                                            startActivity(
                                                Intent(
                                                    Intent.ACTION_VIEW,
                                                    "https://github.com/WholesomeWare/WholesomeWare".toUri()
                                                )
                                            )
                                        },
                                    ) {
                                        Text(text = "App kódja")
                                    }
                                },
                            ),
                        )
                        title("Élő hátterek")
                        items(
                            MenuScope.ItemInfo(
                                onClick = {
                                    setLiveWallpaper(
                                        ComponentName(
                                            this@MainActivity,
                                            KoloraFesztAnalogClockWallpaperService::class.java
                                        )
                                    )
                                },
                                title = "Kolora Feszt analóg óra",
                                description = "Készítette: Csáki",
                                leadingIcon = {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_access_time),
                                        contentDescription = null,
                                    )
                                },
                            ),
                            MenuScope.ItemInfo(
                                onClick = {
                                    setLiveWallpaper(
                                        ComponentName(
                                            this@MainActivity,
                                            TemplateWallpaperService::class.java
                                        )
                                    )
                                },
                                title = "Minta élő háttér",
                                description = "Egy egyszerű példa, ami alapján könnyen lehet új élő hátteret készíteni.",
                            ),
                            MenuScope.ItemInfo(
                                enabled = false,
                                title = "További hátterek hamarosan...",
                            ),
                        )
                        title("Widget-ek")
                        items(
                            MenuScope.ItemInfo(
                                onClick = {
                                    coroutineScope.launch {
                                        GlanceAppWidgetManager(this@MainActivity).requestPinGlanceAppWidget(
                                            receiver = KoloraFesztAnalogClockWidgetReceiver::class.java,
                                            preview = KoloraFesztAnalogClockWidget(),
                                        )
                                    }
                                },
                                title = "Kolora Feszt analóg óra",
                                description = "Készítette: Csáki",
                                leadingIcon = {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_access_time),
                                        contentDescription = null,
                                    )
                                },
                                trailingIcon = {
                                    Badge { Text(text = "Work in progress") }
                                },
                            ),
                        )
                    }
                }
            }
        }
    }

    fun setLiveWallpaper(wallpaperComponentName: ComponentName) {
        try {
            val intent =
                Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER)
            intent.putExtra(
                WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                wallpaperComponentName
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
}
