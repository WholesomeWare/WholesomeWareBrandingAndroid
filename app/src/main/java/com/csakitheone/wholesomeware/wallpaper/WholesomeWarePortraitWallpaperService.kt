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

class WholesomeWarePortraitWallpaperService : WallpaperService() {
    override fun onCreateEngine(): Engine {
        return object : Engine() {
            private lateinit var powerManager: PowerManager
            private val handler = Handler(Looper.getMainLooper())
            private var visible = false
            private var width = 0
            private var height = 0

            private lateinit var backgroundBitmap: Bitmap
            private lateinit var topBitmap: Bitmap
            private lateinit var bottomBitmap: Bitmap
            private lateinit var noiseBitmap: Bitmap

            // Original dimensions of the drawables
            private val viewportWidth = 1080
            private val viewportHeight = 1920

            // Animation variables
            private var time = 0f
            private val animationSpeed = 2f // Controls how fast the motion is
            private val maxYOffset = 10f

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
                // Get drawable resources
                val backgroundDrawable = getDrawable(R.drawable.wholesomeware_portrait_background)
                val topDrawable = getDrawable(R.drawable.wholesomeware_portrait_top)
                val bottomDrawable = getDrawable(R.drawable.wholesomeware_portrait_bottom)
                val noiseDrawable = getDrawable(R.drawable.noise)

                // Convert to bitmaps - use maximum viewport dimensions initially
                val tempBackgroundBitmap = backgroundDrawable?.toBitmap(viewportWidth, viewportHeight)
                val tempTopBitmap = topDrawable?.toBitmap(viewportWidth, viewportHeight)
                val tempBottomBitmap = bottomDrawable?.toBitmap(viewportWidth, viewportHeight)
                val tempNoiseBitmap = noiseDrawable?.toBitmap(viewportWidth, viewportHeight)

                // Calculate scale factor based on screen size
                // Scale to fit the screen while maintaining aspect ratio
                val screenAspectRatio = width.toFloat() / height.toFloat()
                val viewportAspectRatio = viewportWidth.toFloat() / viewportHeight.toFloat()

                val scaleFactor = if (screenAspectRatio > viewportAspectRatio) {
                    // Screen is wider - scale based on height
                    height.toFloat() / viewportHeight.toFloat()
                } else {
                    // Screen is taller or equal aspect - scale based on width
                    width.toFloat() / viewportWidth.toFloat()
                }

                val scaledWidth = (viewportWidth * scaleFactor).toInt()
                val scaledHeight = (viewportHeight * scaleFactor).toInt()

                // Scale the bitmaps
                if (tempBackgroundBitmap != null) {
                    backgroundBitmap = tempBackgroundBitmap.scale(scaledWidth, scaledHeight)
                }
                if (tempTopBitmap != null) {
                    topBitmap = tempTopBitmap.scale(scaledWidth, scaledHeight)
                }
                if (tempBottomBitmap != null) {
                    bottomBitmap = tempBottomBitmap.scale(scaledWidth, scaledHeight)
                }
                if (tempNoiseBitmap != null) {
                    noiseBitmap = tempNoiseBitmap.scale(scaledWidth, scaledHeight)
                }
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

                val backgroundColor = if (isDarkMode) "#1a1a1a" else "#f5f5f5"
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
                // Update animation time
                time += deltaTime / 1000f

                canvas.drawRect(
                    0f,
                    0f,
                    width.toFloat(),
                    height.toFloat(),
                    "#4659A4".toPaint()
                )

                // Calculate center position for the portrait drawables
                val offsetX = (width - backgroundBitmap.width) / 2f
                val offsetY = (height - backgroundBitmap.height) / 2f

                // Calculate vertical movement using sine wave for smooth oscillation
                val backgroundYOffset = sin(time * animationSpeed) * maxYOffset

                // Draw the layers: background, top, bottom
                canvas.drawBitmap(backgroundBitmap, offsetX, offsetY + backgroundYOffset, null)
                // Top should be at top of the screen
                canvas.drawBitmap(topBitmap, offsetX, 0f, null)
                // Bottom should be at bottom of the screen
                canvas.drawBitmap(bottomBitmap, offsetX, height - bottomBitmap.height.toFloat(), null)
            }
        }
    }
}




