package com.csakitheone.wholesomeware.wallpaper

import android.app.WallpaperColors
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.service.wallpaper.WallpaperService
import android.util.Log
import android.view.SurfaceHolder
import androidx.annotation.RequiresApi
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.scale
import androidx.core.graphics.toColorInt
import com.csakitheone.wholesomeware.R
import kotlin.apply
import kotlin.math.max

class LighthouseWallpaperService : WallpaperService() {
    override fun onCreateEngine(): Engine {
        return object : Engine() {
            private lateinit var powerManager: PowerManager
            private val handler = Handler(Looper.getMainLooper())
            private var visible = false
            private var width = 0
            private var height = 0

            private lateinit var lighthouseBitmap: Bitmap
            private lateinit var lighthouseLightBitmap: Bitmap
            private lateinit var birdsBitmap: Bitmap

            private var birdsX = Float.NEGATIVE_INFINITY
            private var batteryPercentage = 100
            private var isCharging = false

            //
            // Helper functions
            // To make your code cleaner, you can add more helper functions here
            //
            /**
             * Convert a hex color string to a Paint object
             */
            private fun String.toPaint(): Paint = Paint().apply {
                color = this@toPaint.toColorInt()
                style = Paint.Style.FILL
                isAntiAlias = true
            }

            private fun updateBitmaps() {
                Log.d("LighthouseWallpaper", "Updating bitmaps for size ${width}x${height}")
                lighthouseBitmap =
                    getDrawable(R.drawable.lighthouse_layer_tower)!!.toBitmap()
                val lighthouseScale = max(
                    width.toFloat() / lighthouseBitmap.width,
                    height.toFloat() / lighthouseBitmap.height
                ) * .8f
                lighthouseBitmap = lighthouseBitmap.scale(
                    (lighthouseBitmap.width * lighthouseScale).toInt(),
                    (lighthouseBitmap.height * lighthouseScale).toInt()
                )
                lighthouseLightBitmap =
                    getDrawable(R.drawable.lighthouse_layer_light)!!.toBitmap()
                        .scale(
                            lighthouseBitmap.width,
                            lighthouseBitmap.height
                        )
                birdsBitmap =
                    getDrawable(R.drawable.lighthouse_layer_birds)!!.toBitmap()
                birdsBitmap = birdsBitmap.scale(
                    (birdsBitmap.width * .5f).toInt(),
                    (birdsBitmap.height * .5f).toInt()
                )

                Log.d(
                    "LighthouseWallpaper",
                    "Bitmaps updated. Lighthouse size: ${lighthouseBitmap.width}x${lighthouseBitmap.height}, Light size: ${lighthouseLightBitmap.width}x${lighthouseLightBitmap.height}, Birds size: ${birdsBitmap.width}x${birdsBitmap.height}"
                )
            }

            private val drawRunnable = object : Runnable {
                override fun run() {
                    val framerate = if (powerManager.isPowerSaveMode) 30 else 120
                    updateBatteryInfo()
                    draw(1000L / framerate)
                    if (visible) {
                        handler.postDelayed(this, 1000L / framerate)
                    }
                }
            }

            private fun updateBatteryInfo() {
                val batteryStatus = registerReceiver(
                    null,
                    IntentFilter(Intent.ACTION_BATTERY_CHANGED)
                )
                val level = batteryStatus?.getIntExtra("level", -1) ?: -1
                val scale = batteryStatus?.getIntExtra("scale", -1) ?: -1
                batteryPercentage = if (level != -1 && scale != -1) {
                    (level * 100) / scale
                } else {
                    -1
                }
                val status = batteryStatus?.getIntExtra("status", -1) ?: -1
                isCharging = status == 2 || status == 5 // BATTERY_STATUS_CHARGING || BATTERY_STATUS_FULL
            }

            //
            // Lifecycle
            // To manage the wallpaper lifecycle, changes and user interactions
            //
            override fun onCreate(surfaceHolder: SurfaceHolder?) {
                super.onCreate(surfaceHolder)
                powerManager = getSystemService(PowerManager::class.java)
            }

            override fun onVisibilityChanged(visible: Boolean) {
                this.visible = visible
                if (visible) {
                    updateBitmaps()
                    handler.post(drawRunnable)
                } else {
                    handler.removeCallbacks(drawRunnable)
                }
            }

            override fun onSurfaceChanged(
                holder: SurfaceHolder?,
                format: Int,
                width: Int,
                height: Int
            ) {
                this.width = width
                this.height = height
                super.onSurfaceChanged(holder, format, width, height)

                updateBitmaps()

                // Notify system about color changes for Material You theming
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                    notifyColorsChanged()
                }
            }

            @RequiresApi(Build.VERSION_CODES.O_MR1)
            override fun onComputeColors(): WallpaperColors {
                val isDarkMode = (resources.configuration.uiMode and
                        Configuration.UI_MODE_NIGHT_MASK) ==
                        Configuration.UI_MODE_NIGHT_YES

                val backgroundColor = if (isDarkMode) "#001a33" else "#87CEEB"
                val foregroundColor = if (isDarkMode) "#87CEEB" else "#001a33"

                return WallpaperColors(
                    Color.valueOf(backgroundColor.toColorInt()),
                    Color.valueOf(foregroundColor.toColorInt()),
                    null
                )
            }

            override fun onSurfaceDestroyed(holder: SurfaceHolder?) {
                super.onSurfaceDestroyed(holder)
                visible = false
                handler.removeCallbacks(drawRunnable)
            }

            //
            // Drawing
            //
            private fun draw(deltaTime: Long) {
                val holder = surfaceHolder
                var canvas: Canvas? = null
                try {
                    canvas = holder.lockCanvas()
                    if (canvas != null) {
                        drawCanvas(canvas, deltaTime)
                    }
                } finally {
                    if (canvas != null) {
                        holder.unlockCanvasAndPost(canvas)
                    }
                }
            }

            private fun drawCanvas(canvas: Canvas, deltaTime: Long) {
                val isDarkMode = (resources.configuration.uiMode and
                        Configuration.UI_MODE_NIGHT_MASK) ==
                        Configuration.UI_MODE_NIGHT_YES

                // Clear background - sky/sea
                val backgroundColor = if (isDarkMode) "#001a33" else "#87CEEB"
                canvas.drawRect(
                    0f,
                    0f,
                    width.toFloat(),
                    height.toFloat(),
                    backgroundColor.toPaint()
                )

                val centerX = width / 2f
                val centerY = height / 2f

                // Draw sea
                val seaColor = if (isDarkMode) "#002147" else "#4682B4"
                canvas.drawRect(
                    0f,
                    centerY + height / 4f,
                    width.toFloat(),
                    height.toFloat(),
                    seaColor.toPaint()
                )


                if (isDarkMode) {
                    // Light
                    val lightPaint = Paint().apply {
                        alpha = if (isCharging) 255
                        else (batteryPercentage * 2.55f).toInt().coerceIn(0, 255)
                    }
                    canvas.drawBitmap(
                        lighthouseLightBitmap,
                        centerX - lighthouseLightBitmap.width / 2f,
                        (centerY + height / 4f) - lighthouseLightBitmap.height * .7f,
                        lightPaint
                    )
                    birdsX = canvas.width.toFloat()
                } else {
                    // Birds
                    if (birdsX > -canvas.width) {
                        canvas.drawBitmap(
                            birdsBitmap,
                            birdsX,
                            height / 8f,
                            null
                        )
                        birdsX -= .1f * deltaTime
                    } else {
                        birdsX = canvas.width.toFloat() * (if ((0..1000).random() < 2) 1 else -1)
                    }
                }

                // Lighthouse
                canvas.drawBitmap(
                    lighthouseBitmap,
                    centerX - lighthouseBitmap.width / 2f,
                    (centerY + height / 4f) - lighthouseBitmap.height * .7f,
                    null
                )
            }
        }
    }
}

