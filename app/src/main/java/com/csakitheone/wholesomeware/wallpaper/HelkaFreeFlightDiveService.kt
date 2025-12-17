package com.csakitheone.wholesomeware.wallpaper

import android.app.WallpaperColors
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import androidx.annotation.RequiresApi
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.scale
import androidx.core.graphics.toColorInt
import com.csakitheone.wholesomeware.R

class HelkaFreeFlightDiveService : WallpaperService() {
    override fun onCreateEngine(): Engine {
        return object : Engine() {
            private val handler = Handler(Looper.getMainLooper())
            private var visible = false
            private var width = 0
            private var height = 0

            private fun String.toPaint(): Paint = Paint().apply {
                color = this@toPaint.toColorInt()
                style = Paint.Style.FILL
                isAntiAlias = true
            }

            // Runner to update wallpaper
            private val drawRunnable = object : Runnable {
                override fun run() {
                    draw()
                    if (visible) {
                        handler.postDelayed(this, 500L)
                    }
                }
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
                draw()
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

                val backgroundColor = if (isDarkMode) "#080d10" else "#fbd5ae"
                val foregroundColor = if (isDarkMode) "#fbd5ae" else "#2f4e5d"

                return WallpaperColors(
                    Color.valueOf(foregroundColor.toColorInt()),
                    Color.valueOf(backgroundColor.toColorInt()),
                    null
                )
            }

            private fun draw() {
                val holder = surfaceHolder
                var canvas: Canvas? = null
                try {
                    canvas = holder.lockCanvas()
                    if (canvas != null) {
                        drawCanvas(canvas)
                    }
                } finally {
                    if (canvas != null) {
                        holder.unlockCanvasAndPost(canvas)
                    }
                }
            }

            private fun drawCanvas(canvas: Canvas) {
                // Example: Dark mode detection
                val isDarkMode = (resources.configuration.uiMode and
                        Configuration.UI_MODE_NIGHT_MASK) ==
                        Configuration.UI_MODE_NIGHT_YES

                // Clear background
                val backgroundColor = if (isDarkMode) "#080d10" else "#fbd5ae"
                val foregroundColor = if (isDarkMode) "#fbd5ae" else "#2f4e5d"
                canvas.drawRect(
                    0f,
                    0f,
                    width.toFloat(),
                    height.toFloat(),
                    backgroundColor.toPaint()
                )

                val bitmap = getDrawable(R.drawable.helka_szabad_repules_merules)?.toBitmap()

                // Draw bitmap centered with tint
                bitmap?.let {
                    // Scale down wallpaper to fit inside screen but keep aspect ratio
                    val scale = minOf(
                        width.toFloat() / it.width,
                        height.toFloat() / it.height
                    )
                    val scaledBitmap = it.scale(
                        (it.width * scale).toInt(),
                        (it.height * scale).toInt()
                    )
                    val left = (width - scaledBitmap.width) / 2f
                    val top = (height - scaledBitmap.height) / 2f

                    val paint = Paint().apply {
                        colorFilter = PorterDuffColorFilter(
                            foregroundColor.toColorInt(),
                            PorterDuff.Mode.SRC_IN
                        )
                    }

                    canvas.drawBitmap(scaledBitmap, left, top, paint)
                }
            }
        }
    }
}

