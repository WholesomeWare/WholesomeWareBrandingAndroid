package com.csakitheone.wholesomeware.wallpaper

import android.app.WallpaperColors
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
import android.view.SurfaceHolder
import androidx.annotation.RequiresApi
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.scale
import androidx.core.graphics.toColorInt
import com.csakitheone.wholesomeware.R
import kotlin.math.sin
import androidx.core.graphics.withTranslation
import kotlin.math.max

class SkeletonSwingWallpaperService : WallpaperService() {
    override fun onCreateEngine(): Engine {
        return object : Engine() {
            private lateinit var powerManager: PowerManager
            private val handler = Handler(Looper.getMainLooper())
            private var visible = false
            private var width = 0
            private var height = 0

            private lateinit var skeletonBitmap: Bitmap
            private var swingAngle = 0f
            private var time = 0f

            // Swing parameters
            private val swingSpeed = 1f // Slower swing
            private val maxSwingAngle = 3f // Maximum swing angle in degrees

            //
            // Helper functions
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
                skeletonBitmap = getDrawable(R.drawable.skeleton_swing_foreground)!!.toBitmap()

                val scale = max(height, 1) * .9f / skeletonBitmap.height.toFloat()
                skeletonBitmap = skeletonBitmap.scale(
                    (skeletonBitmap.width * scale).toInt(),
                    (skeletonBitmap.height * scale).toInt()
                )
            }

            private val drawRunnable = object : Runnable {
                override fun run() {
                    val framerate = if (powerManager.isPowerSaveMode) 30 else 60
                    draw(1000L / framerate)
                    if (visible) {
                        handler.postDelayed(this, 1000L / framerate)
                    }
                }
            }

            //
            // Lifecycle
            //
            override fun onCreate(surfaceHolder: SurfaceHolder?) {
                super.onCreate(surfaceHolder)
                powerManager = getSystemService(PowerManager::class.java)
            }

            override fun onVisibilityChanged(visible: Boolean) {
                this.visible = visible
                if (visible) {
                    if (width > 0 && height > 0) {
                        updateBitmaps()
                    }
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

                val backgroundColor = if (isDarkMode) "#0a0a0a" else "#1a1a1a"
                val foregroundColor = if (isDarkMode) "#808080" else "#606060"

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

                // Update time for animation
                time += deltaTime / 1000f

                // Calculate swing angle using sine wave for smooth pendulum motion
                swingAngle = sin(time * swingSpeed) * maxSwingAngle

                // Clear background with dark color
                val backgroundColor = if (isDarkMode) "#121212" else "#282828"
                canvas.drawRect(
                    0f,
                    0f,
                    width.toFloat(),
                    height.toFloat(),
                    backgroundColor.toPaint()
                )

                // Calculate skeleton position (centered horizontally, upper part of screen)
                val centerX = width / 2f

                // Save canvas state
                canvas.withTranslation(centerX, 0f) {
                    // Move to pivot point
                    // Rotate around the pivot point
                    rotate(swingAngle)

                    // Draw skeleton (offset so rotation happens at top center of bitmap)
                    drawBitmap(
                        skeletonBitmap,
                        -skeletonBitmap.width / 2f,
                        0f,
                        null
                    )

                    // Restore canvas state
                }
            }
        }
    }
}

