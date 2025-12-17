package com.csakitheone.wholesomeware.wallpaper

import android.app.WallpaperColors
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.service.wallpaper.WallpaperService
import android.view.MotionEvent
import android.view.SurfaceHolder
import androidx.annotation.RequiresApi
import java.util.Calendar
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import androidx.core.graphics.toColorInt
import kotlin.apply
import kotlin.math.PI
import kotlin.math.max

class TemplateWallpaperService : WallpaperService() {
    override fun onCreateEngine(): Engine {
        return object : Engine() {
            private lateinit var powerManager: PowerManager
            private val handler = Handler(Looper.getMainLooper())
            private var visible = false
            private var width = 0
            private var height = 0
            private var isTouching = false

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

            // Example: Battery percentage retrieval
            private fun getBatteryPercentage(): Int {
                val batteryStatus = registerReceiver(
                    null,
                    IntentFilter(Intent.ACTION_BATTERY_CHANGED)
                )
                val level = batteryStatus?.getIntExtra("level", -1) ?: -1
                val scale = batteryStatus?.getIntExtra("scale", -1) ?: -1
                return if (level != -1 && scale != -1) {
                    (level * 100) / scale
                } else {
                    -1
                }
            }

            private val drawRunnable = object : Runnable {
                override fun run() {
                    //TODO: Adjust framerate based on wallpaper complexity
                    val framerate = if (powerManager.isPowerSaveMode) 20 else 60
                    draw(1000L / framerate)
                    if (visible) {
                        handler.postDelayed(this, 1000L / framerate)
                    }
                }
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

                val backgroundColor = if (isDarkMode) "#121212" else "#808080"
                val foregroundColor = if (isDarkMode) "#808080" else "#121212"

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

            override fun onTouchEvent(event: MotionEvent?) {
                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> isTouching = true
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> isTouching = false
                }
                super.onTouchEvent(event)
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

            /**
             * Draw on the wallpaper's canvas
             * @param canvas The canvas to draw on
             * @param deltaTime Time passed since last frame in milliseconds
             */
            private fun drawCanvas(canvas: Canvas, deltaTime: Long) {
                // Example: Dark mode detection
                val isDarkMode = (resources.configuration.uiMode and
                        Configuration.UI_MODE_NIGHT_MASK) ==
                        Configuration.UI_MODE_NIGHT_YES

                // Clear background
                val backgroundColor = if (isDarkMode) "#121212" else "#808080"
                canvas.drawRect(
                    0f,
                    0f,
                    width.toFloat(),
                    height.toFloat(),
                    backgroundColor.toPaint()
                )

                //TODO: Draw your wallpaper here. Use the canvas' methods to draw shapes, text, images, etc.

                // This example draws some text indicating the current theme and touch state
                val textPaint = Paint().apply {
                    color = if (isDarkMode) "#FFFFFF".toColorInt() else "#000000".toColorInt()
                    textSize = 50f
                    isAntiAlias = true
                }
                canvas.drawText("Theme: ${if (isDarkMode) "Dark" else "Light"}", 50f, height / 2f - 200f, textPaint)
                canvas.drawText("Touching: $isTouching", 50f, height / 2f, textPaint)
                canvas.drawText("Battery percentage: ${getBatteryPercentage()}%", 50f, height / 2f + 200f, textPaint)
            }
        }
    }
}

