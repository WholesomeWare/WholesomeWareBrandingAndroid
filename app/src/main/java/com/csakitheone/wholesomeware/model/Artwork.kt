package com.csakitheone.wholesomeware.model

import android.app.Activity
import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import com.csakitheone.wholesomeware.wallpaper.D20WallpaperService
import com.csakitheone.wholesomeware.wallpaper.HelkaFreeFlightDiveWallpaperService
import com.csakitheone.wholesomeware.wallpaper.KoloraFesztAnalogClockWallpaperService
import com.csakitheone.wholesomeware.wallpaper.LighthouseWallpaperService
import com.csakitheone.wholesomeware.widget.KoloraFesztAnalogClockWidget
import com.csakitheone.wholesomeware.widget.KoloraFesztAnalogClockWidgetReceiver
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.math.abs

interface Artwork {
    val title: String
    val artist: Artist
    val description: String
    val unlockDescription: String
    val unlockData: String
    val tags: List<String>
    val permissions: List<String>
}

data class WallpaperArtwork(
    override val title: String,
    override val artist: Artist,
    override val description: String = "",
    override val unlockDescription: String = "",
    override val unlockData: String = "",
    override val tags: List<String> = emptyList(),
    override val permissions: List<String> = emptyList(),
    val componentName: ComponentName,
) : Artwork {
    fun set(context: Activity) {
        val missingPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(
                context,
                it
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            context.requestPermissions(permissions.toTypedArray(), abs(hashCode()))
            return
        }

        try {
            val intent =
                Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER)
            intent.putExtra(
                WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                componentName
            )
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "Error setting wallpaper: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}

data class WidgetArtwork<T>(
    override val title: String,
    override val artist: Artist,
    override val description: String = "",
    override val unlockDescription: String = "",
    override val unlockData: String = "",
    override val tags: List<String> = emptyList(),
    override val permissions: List<String> = emptyList(),
    val receiver: Class<T>,
    val widget: GlanceAppWidget,
): Artwork {
    fun set(context: Activity) {
        val missingPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(
                context,
                it
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            context.requestPermissions(permissions.toTypedArray(), abs(hashCode()))
            return
        }

        GlobalScope.launch {
            GlanceAppWidgetManager(context).requestPinGlanceAppWidget(
                receiver = KoloraFesztAnalogClockWidgetReceiver::class.java,
                preview = KoloraFesztAnalogClockWidget(),
            )
        }
    }
}

fun getArtworks(context: Context): List<Artwork> {
    return listOf(
        WallpaperArtwork(
            title = "D20",
            artist = ARTIST_CSAKI,
            description = "Koppints duplán a kocka megforgatásához!",
            componentName = ComponentName(
                context,
                D20WallpaperService::class.java
            ),
        ),
        WallpaperArtwork(
            title = "Kolora Feszt óra",
            artist = ARTIST_CSAKI,
            description = "Analóg óra a 2025-ös Kolora Feszt plakátja stílusában.",
            tags = listOf("kolora"),
            componentName = ComponentName(
                context,
                KoloraFesztAnalogClockWallpaperService::class.java
            ),
        ),
        WallpaperArtwork(
            title = "szabad repülés - szabad merülés",
            artist = ARTIST_HELKA,
            description = "Helka két kislemez borítójából készült grafika.",
            unlockDescription = "Feloldáshoz látogasd meg Helka weboldalát.",
            unlockData = "https://www.helkamusic.hu/",
            tags = listOf("rajz"),
            permissions = listOf(
                android.Manifest.permission.RECORD_AUDIO,
                android.Manifest.permission.MODIFY_AUDIO_SETTINGS,
            ),
            componentName = ComponentName(
                context,
                HelkaFreeFlightDiveWallpaperService::class.java
            ),
        ),
        WallpaperArtwork(
            title = "Világítótorony",
            artist = ARTIST_M_LIA,
            description = "Fények a sötétben és repülő madarak a világosban.",
            tags = listOf("rajz"),
            componentName = ComponentName(
                context,
                LighthouseWallpaperService::class.java
            ),
        ),
        WidgetArtwork(
            title = "Kolora Feszt óra",
            artist = ARTIST_CSAKI,
            description = "Analóg óra a 2025-ös Kolora Feszt plakátja stílusában.",
            tags = listOf("kolora"),
            receiver = KoloraFesztAnalogClockWidgetReceiver::class.java,
            widget = KoloraFesztAnalogClockWidget(),
        ),
    )
}
