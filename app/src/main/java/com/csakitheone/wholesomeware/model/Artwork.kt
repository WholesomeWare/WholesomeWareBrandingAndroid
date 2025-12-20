package com.csakitheone.wholesomeware.model

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import com.csakitheone.wholesomeware.wallpaper.D20WallpaperService
import com.csakitheone.wholesomeware.wallpaper.HelkaFreeFlightDiveService
import com.csakitheone.wholesomeware.wallpaper.KoloraFesztAnalogClockWallpaperService
import com.csakitheone.wholesomeware.wallpaper.LighthouseWallpaperService
import com.csakitheone.wholesomeware.wallpaper.TemplateWallpaperService
import com.csakitheone.wholesomeware.widget.KoloraFesztAnalogClockWidget
import com.csakitheone.wholesomeware.widget.KoloraFesztAnalogClockWidgetReceiver
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

interface Artwork {
    val title: String
    val author: String
    val description: String
    val unlockDescription: String
    val unlockData: String
}

data class WallpaperArtwork(
    override val title: String,
    override val author: String,
    override val description: String = "",
    override val unlockDescription: String = "",
    override val unlockData: String = "",
    val componentName: ComponentName,
) : Artwork {
    fun set(context: Context) {
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
    override val author: String,
    override val description: String = "",
    override val unlockDescription: String = "",
    override val unlockData: String = "",
    val receiver: Class<T>,
    val widget: GlanceAppWidget,
): Artwork {
    fun set(context: Context) {
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
            author = "Csáki",
            description = "Koppints duplán a kocka megforgatásához!",
            componentName = ComponentName(
                context,
                D20WallpaperService::class.java
            ),
        ),
        WallpaperArtwork(
            title = "Kolora Feszt óra",
            author = "Csáki",
            description = "Analóg óra a 2025-ös Kolora Feszt plakátja stílusában.",
            componentName = ComponentName(
                context,
                KoloraFesztAnalogClockWallpaperService::class.java
            ),
        ),
        WallpaperArtwork(
            title = "szabad repülés - szabad merülés",
            author = "Helka",
            description = "Helka két kislemez borítójából készült grafika.",
            unlockDescription = "Feloldáshoz látogasd meg Helka weboldalát.",
            unlockData = "https://www.helkamusic.hu/",
            componentName = ComponentName(
                context,
                HelkaFreeFlightDiveService::class.java
            ),
        ),
        WallpaperArtwork(
            title = "Világítótorony",
            author = "M. Lia",
            componentName = ComponentName(
                context,
                LighthouseWallpaperService::class.java
            ),
        ),
        WidgetArtwork(
            title = "Kolora Feszt óra",
            author = "Csáki",
            description = "Analóg óra a 2025-ös Kolora Feszt plakátja stílusában.",
            receiver = KoloraFesztAnalogClockWidgetReceiver::class.java,
            widget = KoloraFesztAnalogClockWidget(),
        ),
    )
}
