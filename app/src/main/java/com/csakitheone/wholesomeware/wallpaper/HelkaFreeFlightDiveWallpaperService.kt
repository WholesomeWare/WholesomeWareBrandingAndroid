package com.csakitheone.wholesomeware.wallpaper

import android.app.WallpaperColors
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.media.audiofx.Visualizer
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

class HelkaFreeFlightDiveWallpaperService : WallpaperService() {
    override fun onCreateEngine(): Engine {
        return object : Engine() {
            private lateinit var powerManager: PowerManager
            private val handler = Handler(Looper.getMainLooper())
            private var visible = false
            private var width = 0
            private var height = 0

            private var bitmap: Bitmap? = null
            private var visualizer: Visualizer? = null
            private var visualizerData: ByteArray = ByteArray(128) { 0 }

            private fun String.toPaint(): Paint = Paint().apply {
                color = this@toPaint.toColorInt()
                style = Paint.Style.FILL
                isAntiAlias = true
            }

            private val drawRunnable = object : Runnable {
                override fun run() {
                    val framerate = if (powerManager.isPowerSaveMode) 1 else 60
                    draw()
                    if (visible) {
                        handler.postDelayed(this, 1000L / framerate)
                    }
                }
            }

            override fun onCreate(surfaceHolder: SurfaceHolder?) {
                super.onCreate(surfaceHolder)
                bitmap = getDrawable(R.drawable.helka_szabad_repules_merules)?.toBitmap()
                powerManager = getSystemService(PowerManager::class.java)
            }

            override fun onVisibilityChanged(visible: Boolean) {
                this.visible = visible
                visualizer?.release()
                if (visible) {
                    bitmap = getDrawable(R.drawable.helka_szabad_repules_merules)?.toBitmap()
                    visualizer = Visualizer(0).apply {
                        enabled = false
                        captureSize = Visualizer.getCaptureSizeRange()[0]
                        setDataCaptureListener(object : Visualizer.OnDataCaptureListener {
                            override fun onWaveFormDataCapture(
                                visualizer: Visualizer?,
                                waveform: ByteArray?,
                                samplingRate: Int
                            ) {
                                visualizerData = waveform ?: ByteArray(128) { 0 }
                            }

                            override fun onFftDataCapture(
                                visualizer: Visualizer?,
                                fft: ByteArray?,
                                samplingRate: Int
                            ) {
                                // Not used
                            }
                        }, Visualizer.getMaxCaptureRate(), true, false)
                        enabled = true
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

                val backgroundColor = if (isDarkMode) "#1c0b06" else "#fbd5ae"
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
                val isDarkMode = (resources.configuration.uiMode and
                        Configuration.UI_MODE_NIGHT_MASK) ==
                        Configuration.UI_MODE_NIGHT_YES

                val backgroundColor = if (isDarkMode) "#1c0b06" else "#fbd5ae"
                val foregroundColor = if (isDarkMode) "#fbd5ae" else "#2f4e5d"
                canvas.drawRect(
                    0f,
                    0f,
                    width.toFloat(),
                    height.toFloat(),
                    backgroundColor.toPaint()
                )

                if (!isDarkMode) {
                    val vignettePaint = Paint().apply {
                        shader = android.graphics.RadialGradient(
                            width / 2f,
                            height / 2f,
                            (width.coerceAtLeast(height) / 1.5).toFloat(),
                            intArrayOf(Color.TRANSPARENT, 0x88000000.toInt()),
                            floatArrayOf(0.4f, 1.0f),
                            android.graphics.Shader.TileMode.CLAMP
                        )
                    }
                    canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), vignettePaint)
                }

                bitmap?.let {
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

                    // Draw audio visualizer
                    val dotWidth = scaledBitmap.width / 5 / visualizerData.size.toFloat()
                    for (i in visualizerData.indices) {
                        val magnitudeXModifier = 1f - (i.toFloat() / visualizerData.size.toFloat())
                        val magnitude = (visualizerData[i] + 128).toFloat() / 256f * magnitudeXModifier
                        val x = left + scaledBitmap.width * .08f + i * dotWidth
                        val x2 = left + scaledBitmap.width * .92f - i * dotWidth
                        val y = top + scaledBitmap.height / 2.08f - magnitude * scaledBitmap.height / 32
                        canvas.drawRect(
                            x,
                            y - dotWidth,
                            x + dotWidth,
                            y + dotWidth,
                            foregroundColor.toPaint()
                        )
                        canvas.drawRect(
                            x2,
                            y - dotWidth,
                            x2 + dotWidth,
                            y + dotWidth,
                            foregroundColor.toPaint()
                        )
                    }
                }
            }
        }
    }
}

