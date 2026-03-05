package com.csakitheone.wholesomeware

import android.app.SearchManager
import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.material3.ToggleFloatingActionButtonDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.csakitheone.wholesomeware.ui.components.Menu
import com.csakitheone.wholesomeware.ui.components.MenuScope
import com.csakitheone.wholesomeware_brand.ui.theme.WholesomewareBrandTheme
import androidx.core.net.toUri
import com.csakitheone.wholesomeware.experiment.ChaosToCalendarExperimentActivity
import com.csakitheone.wholesomeware.experiment.DartsHelper
import com.csakitheone.wholesomeware.experiment.NetworkUtils
import com.csakitheone.wholesomeware.experiment.RadioExperimentActivity
import com.csakitheone.wholesomeware.model.Artwork
import com.csakitheone.wholesomeware.model.WallpaperArtwork
import com.csakitheone.wholesomeware.model.WidgetArtwork
import com.csakitheone.wholesomeware.model.getArtworks
import com.csakitheone.wholesomeware.service.RadioService
import com.csakitheone.wholesomeware.ui.components.WWMenuDefaults
import com.csakitheone.wholesomeware_brand.WholesomeWare
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.URL
import kotlin.math.min

class MainActivity : ComponentActivity() {

    private val TAB_HOME = "home"
    private val TAB_ARTWORKS = "artworks"
    private val TAB_EXPERIMENTS = "experiments"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        enableEdgeToEdge()
        setContent {
            MainScreen()
        }

        askNotifyPermission()
    }

    private fun askNotifyPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
        }
    }

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    @Preview
    private fun MainScreen() {
        WholesomewareBrandTheme {
            val resources = LocalResources.current
            val density = LocalDensity.current

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

            var isMenuOpen by rememberSaveable { mutableStateOf(false) }
            var selectedTab by rememberSaveable { mutableStateOf(TAB_HOME) }

            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .nestedScroll(nestedScrollConnection),
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
                                        headerMinHeight =
                                            min(it.size.height.toFloat(), headerMaxHeight - 1f)
                                    },
                                title = { Text(text = "WholesomeWare") },
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
                                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                                ),
                            )
                            TopAppBar(
                                title = {},
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = Color.Transparent,
                                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
                                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                                ),
                                actions = {
                                    FilledIconButton(
                                        onClick = {
                                            val shareLink =
                                                "https://play.google.com/store/apps/details?id=com.csakitheone.wholesomeware"
                                            startActivity(
                                                Intent.createChooser(
                                                    Intent(Intent.ACTION_SEND).apply {
                                                        type = "text/plain"
                                                        putExtra(
                                                            Intent.EXTRA_SUBJECT,
                                                            "Próbáld ki a WholesomeWare appot!"
                                                        )
                                                        putExtra(
                                                            Intent.EXTRA_TEXT,
                                                            shareLink
                                                        )
                                                    },
                                                    "Megosztás"
                                                )
                                            )
                                        },
                                    ) {
                                        Icon(
                                            painter = painterResource(id = androidx.media3.session.R.drawable.media3_icon_share),
                                            contentDescription = null,
                                        )
                                    }
                                    FilledIconButton(
                                        onClick = { isMenuOpen = true },
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_more_vert),
                                            contentDescription = null,
                                        )
                                        DropdownMenu(
                                            expanded = isMenuOpen,
                                            onDismissRequest = { isMenuOpen = false },
                                        ) {
                                            DropdownMenuItem(
                                                text = { Text(text = "App Play Áruház oldala") },
                                                onClick = {
                                                    startActivity(
                                                        Intent(
                                                            Intent.ACTION_VIEW,
                                                            "https://play.google.com/store/apps/details?id=com.csakitheone.wholesomeware".toUri()
                                                        )
                                                    )
                                                },
                                            )
                                            HorizontalDivider()
                                            DropdownMenuItem(
                                                text = { Text(text = "GitHub") },
                                                onClick = {
                                                    startActivity(
                                                        Intent(
                                                            Intent.ACTION_VIEW,
                                                            "https://github.com/WholesomeWare".toUri()
                                                        )
                                                    )
                                                    isMenuOpen = false
                                                },
                                                leadingIcon = {
                                                    Icon(
                                                        painter = painterResource(id = R.drawable.ic_github),
                                                        contentDescription = null,
                                                    )
                                                },
                                            )
                                            DropdownMenuItem(
                                                text = { Text(text = "App kódja") },
                                                onClick = {
                                                    startActivity(
                                                        Intent(
                                                            Intent.ACTION_VIEW,
                                                            "https://github.com/WholesomeWare/WholesomeWareBrandingAndroid".toUri()
                                                        )
                                                    )
                                                    isMenuOpen = false
                                                },
                                                leadingIcon = {
                                                    Icon(
                                                        painter = painterResource(id = R.drawable.ic_code),
                                                        contentDescription = null,
                                                    )
                                                },
                                            )
                                        }
                                    }
                                },
                            )
                        }
                    }
                    AnimatedContent(
                        modifier = Modifier.weight(1f),
                        targetState = selectedTab,
                    ) {
                        when (it) {
                            TAB_HOME -> TabHome(
                                modifier = Modifier.verticalScroll(menuScrollState),
                                onTabChangeRequest = { tab -> selectedTab = tab }
                            )

                            TAB_ARTWORKS -> TabArtworks(
                                modifier = Modifier.verticalScroll(menuScrollState),
                            )

                            TAB_EXPERIMENTS -> TabExperiments(
                                modifier = Modifier.verticalScroll(menuScrollState),
                            )
                        }
                    }
                    NavigationBar {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            NavigationBarItem(
                                selected = selectedTab == TAB_HOME,
                                onClick = { selectedTab = TAB_HOME },
                                icon = {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_home),
                                        contentDescription = null,
                                    )
                                },
                                label = { Text(text = "Főoldal") },
                            )
                            NavigationBarItem(
                                selected = selectedTab == TAB_ARTWORKS,
                                onClick = { selectedTab = TAB_ARTWORKS },
                                icon = {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_palette),
                                        contentDescription = null,
                                    )
                                },
                                label = { Text(text = "Alkotások") },
                            )
                            NavigationBarItem(
                                selected = selectedTab == TAB_EXPERIMENTS,
                                onClick = { selectedTab = TAB_EXPERIMENTS },
                                icon = {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_experiment),
                                        contentDescription = null,
                                    )
                                },
                                label = { Text(text = "Kísérletek") },
                            )
                        }
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    private fun TabHome(
        modifier: Modifier = Modifier,
        onTabChangeRequest: (String) -> Unit = { _ -> },
    ) {
        val context = LocalContext.current

        Menu(modifier = modifier, contentPadding = PaddingValues(16.dp)) {
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
                    text = "A WholesomeWare app digitális alkotások gyűjteménye, amelyeket barátaim vagy jómagam készítettek.",
                )
            }
            WWMenuDefaults.sectionSpacer()
            items(
                MenuScope.ItemInfo(
                    onClick = { onTabChangeRequest(TAB_ARTWORKS) },
                    title = "Alkotások",
                    description = "Élő hátterek és widget-ek",
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_palette),
                            contentDescription = null,
                        )
                    },
                ),
                MenuScope.ItemInfo(
                    onClick = { onTabChangeRequest(TAB_EXPERIMENTS) },
                    title = "Kísérletek",
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_experiment),
                            contentDescription = null,
                        )
                    },
                ),
                MenuScope.ItemInfo(
                    onClick = { WholesomeWare.openPlayStore(this@MainActivity) },
                    title = "Mégtöbb app tőlünk",
                    leadingIcon = {
                        Icon(
                            painter = painterResource(com.csakitheone.wholesomeware_brand.R.drawable.ic_wholesomeware),
                            contentDescription = null,
                        )
                    },
                    trailingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_open_in_new),
                            contentDescription = null
                        )
                    },
                    backgroundImage = painterResource(R.drawable.wholesomeware_banner_notext),
                ),
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
                    description = "A WW nem hivatalos szülő szervezete",
                    leadingIcon = {
                        Icon(
                            modifier = Modifier.size(24.dp),
                            painter = painterResource(id = R.drawable.ic_kolora),
                            contentDescription = null,
                        )
                    },
                    trailingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_open_in_new),
                            contentDescription = null
                        )
                    },
                    backgroundImage = painterResource(R.drawable.kolora_banner),
                )
            )
        }
    }

    @Composable
    private fun TabArtworks(
        modifier: Modifier = Modifier,
    ) {
        val context = LocalContext.current
        val activity = LocalActivity.current
        val artworks = remember { getArtworks(context) }

        var selectedArtwork by remember { mutableStateOf<Artwork?>(null) }
        val isSelectedArtworkUnlocked by remember {
            derivedStateOf {
                if (selectedArtwork == null) return@derivedStateOf false
                UserSettings.isArtworkUnlocked(context, selectedArtwork!!.unlockData)
            }
        }

        if (selectedArtwork != null) {
            AlertDialog(
                onDismissRequest = { selectedArtwork = null },
                title = { Text(text = selectedArtwork!!.title) },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Text(text = "Készítő: ${selectedArtwork!!.artist}")
                        if (selectedArtwork!!.description.isNotEmpty()) {
                            Text(
                                text = selectedArtwork!!.description,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                        if (selectedArtwork!!.tags.isNotEmpty()) {
                            Text(
                                text = selectedArtwork!!.tags.joinToString { "#$it" },
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                        if (!isSelectedArtworkUnlocked) {
                            OutlinedCard {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_lock),
                                        contentDescription = null,
                                    )
                                    Spacer(modifier = Modifier.size(16.dp))
                                    Text(
                                        text = selectedArtwork!!.unlockDescription,
                                        style = MaterialTheme.typography.bodySmall,
                                    )
                                }
                            }
                        } else if (!selectedArtwork?.unlockData.isNullOrBlank()) {
                            OutlinedButton(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = {
                                    UserSettings.lockArtwork(
                                        context,
                                        selectedArtwork!!.unlockData
                                    )
                                    selectedArtwork = null
                                }
                            ) {
                                Icon(
                                    modifier = Modifier.padding(end = ButtonDefaults.IconSpacing),
                                    painter = painterResource(id = R.drawable.ic_lock),
                                    contentDescription = null,
                                )
                                Text(text = "Lezárás")
                            }
                        }
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { selectedArtwork = null }
                    ) {
                        Text(text = "Bezárás")
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (!isSelectedArtworkUnlocked) {
                                startActivity(
                                    Intent(Intent.ACTION_VIEW, selectedArtwork!!.unlockData.toUri())
                                )
                                UserSettings.unlockArtwork(
                                    context,
                                    selectedArtwork!!.unlockData
                                )
                                selectedArtwork = null
                                return@Button
                            }

                            if (activity == null) return@Button

                            selectedArtwork!!.let { artwork ->
                                when (artwork) {
                                    is WallpaperArtwork -> {
                                        artwork.set(activity)
                                    }

                                    is WidgetArtwork<*> -> {
                                        artwork.set(activity)
                                    }
                                }
                            }
                            selectedArtwork = null
                        }
                    ) {
                        Text(text = if (isSelectedArtworkUnlocked) "Beállítás" else "Feloldás")
                    }
                }
            )
        }

        Menu(modifier = modifier, contentPadding = PaddingValues(16.dp)) {
            title("Élő hátterek")
            items(
                artworks.filter { it is WallpaperArtwork }.map { artwork ->
                    MenuScope.ItemInfo(
                        onClick = {
                            if (!UserSettings.isArtworkUnlocked(context, artwork.unlockData)) {
                                selectedArtwork = artwork
                                return@ItemInfo
                            }

                            if (activity == null) return@ItemInfo

                            (artwork as WallpaperArtwork).set(activity)
                        },
                        title = artwork.title,
                        description = "by ${artwork.artist} ${artwork.tags.joinToString { "#$it" }}",
                        leadingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_image),
                                contentDescription = null,
                            )
                        },
                        trailingIcon = {
                            IconButton(
                                onClick = { selectedArtwork = artwork }
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_info),
                                    contentDescription = null,
                                )
                            }
                        },
                        backgroundImage = artwork.previewDrawableResource?.let { painterResource(it) },
                    )
                }
            )
            title("Widget-ek")
            items(
                artworks.filter { it is WidgetArtwork<*> }.map { artwork ->
                    MenuScope.ItemInfo(
                        onClick = {
                            if (!UserSettings.isArtworkUnlocked(context, artwork.unlockData)) {
                                selectedArtwork = artwork
                                return@ItemInfo
                            }

                            if (activity == null) return@ItemInfo

                            (artwork as WidgetArtwork<*>).set(activity)
                        },
                        title = artwork.title,
                        description = "by ${artwork.artist} ${artwork.tags.joinToString { "#$it" }}",
                        leadingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_widgets),
                                contentDescription = null,
                            )
                        },
                        trailingIcon = {
                            IconButton(
                                onClick = { selectedArtwork = artwork }
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_info),
                                    contentDescription = null,
                                )
                            }
                        },
                        backgroundImage = artwork.previewDrawableResource?.let { painterResource(it) },
                    )
                }
            )
            title("Alkoss te is!")
            ElevatedCard {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "Ha van ötleted, rajzod, grafikád vagy bármi, amiből élő hátteret vagy widget-et lehetne készíteni, keress bátran!",
                        style = MaterialTheme.typography.bodySmall,
                    )
                    OutlinedButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    "https://instagram.com/wholesomewarestuff".toUri()
                                )
                            )
                        },
                    ) {
                        Text(text = "WholesomeWare Insta")
                    }
                    OutlinedButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    "https://m.me/CsakiTheOne".toUri()
                                )
                            )
                        },
                    ) {
                        Text(text = "Messenger üzenet Csákinak")
                    }
                    OutlinedButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    "mailto:jockahun@gmail.com".toUri()
                                ).apply {
                                    putExtra(
                                        Intent.EXTRA_SUBJECT,
                                        "WholesomeWare alkotás beküldése"
                                    )
                                }
                            )
                        },
                    ) {
                        Text(text = "E-mail küldése")
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    private fun TabExperiments(
        modifier: Modifier = Modifier,
    ) {
        val context = LocalContext.current

        Menu(modifier = modifier, contentPadding = PaddingValues(16.dp)) {
            ElevatedCard(
                shape = WWMenuDefaults.cardSingleItemShape(),
            ) {
                Text(
                    modifier = Modifier.padding(16.dp),
                    text = "Ha egy barátomnak vagy nekem jön egy ötlet, amit ki szeretnénk próbálni, de fölösleges egy külön alkalmazást készíteni hozzá, akkor ide kerülnek.",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            title("Kísérletek")
            items(
                MenuScope.ItemInfo(
                    onClick = {
                        startActivity(
                            Intent(context, ChaosToCalendarExperimentActivity::class.java)
                        )
                    },
                    title = "Káoszból naptárba",
                    description = "Írj be egy szabad szöveget és a mesterséges intelligencia megpróbálja értelmezni, hogy milyen naptári eseményeket lehetne létrehozni belőle.",
                ),
                MenuScope.ItemInfo(
                    onClick = {
                        startActivity(
                            Intent(context, RadioExperimentActivity::class.java)
                        )
                    },
                    title = "Rádió most játszott megnyitása zenelejátszó appban",
                ),
            )
            /*items(
                MenuScope.ItemInfo(
                    onClick = { isEventRecommendationDialogOpen = true },
                    title = "Esemény ajánló",
                    trailingIcon = {
                        if (isEventRecommendationDialogOpen) {
                            AlertDialog(
                                onDismissRequest = { isEventRecommendationDialogOpen = false },
                                title = { Text(text = "Esemény ajánló") },
                                text = {
                                    if (eventRecommendationSummary == null) {
                                        LaunchedEffect(Unit) {
                                            eventRecommendationSummary =
                                                EventRecommendationAI.getUpcomingEventsSummary()
                                        }
                                        Box(
                                            modifier = Modifier.fillMaxWidth(),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            CircularProgressIndicator()
                                        }
                                    } else {
                                        Text(
                                            modifier = Modifier
                                                .verticalScroll(rememberScrollState()),
                                            text = eventRecommendationSummary
                                                ?: "Sajnálom, nem sikerült ajánlót készíteni."
                                        )
                                    }
                                },
                                confirmButton = {
                                    TextButton(
                                        onClick = {
                                            isEventRecommendationDialogOpen = false
                                        }
                                    ) {
                                        Text(text = "Bezárás")
                                    }
                                },
                            )
                        }
                    },
                ),
            )*/
        }
    }
}
