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
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import com.csakitheone.wholesomeware.R
import com.csakitheone.wholesomeware.wallpaper.D20WallpaperService
import com.csakitheone.wholesomeware.wallpaper.HelkaFreeFlightDiveWallpaperService
import com.csakitheone.wholesomeware.wallpaper.KoloraFesztAnalogClockWallpaperService
import com.csakitheone.wholesomeware.wallpaper.LighthouseWallpaperService
import com.csakitheone.wholesomeware.wallpaper.SkeletonSwingWallpaperService
import com.csakitheone.wholesomeware.wallpaper.WholesomeWarePortraitWallpaperService
import com.csakitheone.wholesomeware.wallpaper.YearCalendarWallpaperService
import com.csakitheone.wholesomeware.widget.DartsHelperWidget
import com.csakitheone.wholesomeware.widget.DartsHelperWidgetReceiver
import com.csakitheone.wholesomeware.widget.FluidIntakeTrackerWidget
import com.csakitheone.wholesomeware.widget.FluidIntakeTrackerWidgetReceiver
import com.csakitheone.wholesomeware.widget.KoloraFesztAnalogClockWidget
import com.csakitheone.wholesomeware.widget.KoloraFesztAnalogClockWidgetReceiver
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.math.abs

interface Artwork {
    val title: String
    val artist: Artist
    val description: String
    val previewDrawableResource: Int?
    val unlockDescription: String
    val unlockData: String
    val tags: List<String>
    val permissions: List<String>
}

data class WallpaperArtwork(
    override val title: String,
    override val artist: Artist,
    override val description: String = "",
    override val previewDrawableResource: Int? = null,
    override val unlockDescription: String = "",
    override val unlockData: String = "",
    override val tags: List<String> = emptyList(),
    override val permissions: List<String> = emptyList(),
    val componentName: ComponentName,
) : Artwork {
    fun set(activity: Activity) {
        val missingPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(
                activity,
                it
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            activity.requestPermissions(permissions.toTypedArray(), abs(hashCode()))
            return
        }

        try {
            val intent =
                Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER)
            intent.putExtra(
                WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                componentName
            )
            activity.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(
                activity,
                "Error setting wallpaper: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}

data class WidgetArtwork<T : GlanceAppWidgetReceiver>(
    override val title: String,
    override val artist: Artist,
    override val description: String = "",
    override val previewDrawableResource: Int? = null,
    override val unlockDescription: String = "",
    override val unlockData: String = "",
    override val tags: List<String> = emptyList(),
    override val permissions: List<String> = emptyList(),
    val receiver: Class<T>,
    val widget: GlanceAppWidget,
): Artwork {
    fun set(activity: Activity) {
        val missingPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(
                activity,
                it
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            activity.requestPermissions(permissions.toTypedArray(), abs(hashCode()))
            return
        }

        GlobalScope.launch {
            GlanceAppWidgetManager(activity).requestPinGlanceAppWidget(
                receiver = receiver,
                preview = widget,
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
            title = "Év naptár",
            artist = ARTIST_CSAKI,
            description = "Hol járunk az évben?",
            componentName = ComponentName(
                context,
                YearCalendarWallpaperService::class.java
            ),
        ),
        WallpaperArtwork(
            title = "Hintázó csontváz",
            artist = ARTIST_UNKNOWN,
            description = "Egy csontváz lassan hintázik a sötétben.",
            previewDrawableResource = R.drawable.skeleton_swing_foreground,
            componentName = ComponentName(
                context,
                SkeletonSwingWallpaperService::class.java
            ),
        ),
        WallpaperArtwork(
            title = "Kolora Feszt óra",
            artist = ARTIST_CSAKI,
            description = "Analóg óra a 2025-ös Kolora Feszt plakátja stílusában.",
            unlockDescription = "Feloldáshoz látogasd meg a Kolora Egyesület weboldalát.",
            unlockData = "https://kolora.web.app/",
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
            previewDrawableResource = R.drawable.helka_szabad_repules_merules,
            unlockDescription = "Feloldáshoz látogasd meg Helka weboldalát.",
            unlockData = "https://www.helkamusic.hu/",
            tags = listOf("rajz", "zene"),
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
            previewDrawableResource = R.drawable.lighthouse_layer_tower,
            tags = listOf("rajz"),
            componentName = ComponentName(
                context,
                LighthouseWallpaperService::class.java
            ),
        ),
        WallpaperArtwork(
            title = "WholesomeWare portré",
            artist = ARTIST_CSAKI,
            description = "A WholesomeWare branding portré illusztrációja.",
            tags = listOf("branding"),
            componentName = ComponentName(
                context,
                WholesomeWarePortraitWallpaperService::class.java
            ),
        ),

        WidgetArtwork(
            title = "Darts: legkevesebb dobás számláló",
            artist = ARTIST_CSAKI,
            description = "Számítsd ki, hogy hány dobással tudsz nyerni dartsban.",
            receiver = DartsHelperWidgetReceiver::class.java,
            widget = DartsHelperWidget(),
        ),
        WidgetArtwork(
            title = "Folyadékbevitel naplózó",
            artist = ARTIST_CSAKI,
            description = "Kövesd nyomon a napi folyadékfogyasztásodat.",
            receiver = FluidIntakeTrackerWidgetReceiver::class.java,
            widget = FluidIntakeTrackerWidget(),
        ),
        WidgetArtwork(
            title = "Kolora Feszt óra",
            artist = ARTIST_CSAKI,
            description = "Analóg óra a 2025-ös Kolora Feszt plakátja stílusában.",
            unlockDescription = "Feloldáshoz látogasd meg a Kolora Egyesület weboldalát.",
            unlockData = "https://kolora.web.app/",
            tags = listOf("kolora"),
            receiver = KoloraFesztAnalogClockWidgetReceiver::class.java,
            widget = KoloraFesztAnalogClockWidget(),
        ),
    )
}
