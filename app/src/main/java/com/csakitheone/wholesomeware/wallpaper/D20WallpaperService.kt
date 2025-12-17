package com.csakitheone.wholesomeware.wallpaper

import android.app.WallpaperColors
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.service.wallpaper.WallpaperService
import android.view.MotionEvent
import android.view.SurfaceHolder
import androidx.annotation.RequiresApi
import androidx.core.graphics.toColorInt
import kotlin.math.cos
import kotlin.math.sin

class D20WallpaperService : WallpaperService() {
    override fun onCreateEngine(): Engine {
        return object : Engine() {
            private var visible = false
            private var width = 0
            private var height = 0

            // Rolling state and animation
            private lateinit var vibrator: Vibrator
            private var rollsQueued = 0
            private var currentDieValue = (1..20).random()

            // Double tap detection
            private var lastTapTime = 0L
            private val doubleTapThreshold = 300L // milliseconds

            // Roll animation handler
            private val rollHandler = Handler(Looper.getMainLooper())
            private val rollRunnable = object : Runnable {
                override fun run() {
                    if (rollsQueued > 0) {
                        currentDieValue = (1..20).random()
                        rollsQueued--
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
                        }
                        draw()
                        rollHandler.postDelayed(this, 50)
                    }
                }
            }

            private fun String.toPaint(): Paint = Paint().apply {
                color = this@toPaint.toColorInt()
                style = Paint.Style.FILL
                isAntiAlias = true
            }

            override fun onCreate(surfaceHolder: SurfaceHolder?) {
                super.onCreate(surfaceHolder)
                vibrator = getSystemService(Vibrator::class.java)
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
                rollHandler.removeCallbacks(rollRunnable)
            }

            override fun onTouchEvent(event: MotionEvent?) {
                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        val currentTime = System.currentTimeMillis()
                        if (currentTime - lastTapTime < doubleTapThreshold) {
                            // Double tap detected
                            rollsQueued += 10
                            // Start the roll animation if not already running
                            if (rollsQueued > 0 && !rollHandler.hasCallbacks(rollRunnable)) {
                                rollHandler.post(rollRunnable)
                            }
                        }
                        lastTapTime = currentTime
                    }
                }
                super.onTouchEvent(event)
            }

            //
            // Drawing
            //
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
                val backgroundColor = if (isDarkMode) "#121212" else "#808080"
                canvas.drawRect(
                    0f,
                    0f,
                    width.toFloat(),
                    height.toFloat(),
                    backgroundColor.toPaint()
                )

                val d20Paint = (when (currentDieValue) {
                    20 -> "#124812"
                    1 -> "#481212"
                    else -> "#282828"
                }).toPaint()
                val d20TextPaint = "#808080".toPaint().apply {
                    textSize = (width.coerceAtMost(height) / 4f)
                    textAlign = Paint.Align.CENTER
                }

                // D20 outline / 6 sided polygon
                val centerX = width / 2f
                val centerY = height / 2f
                val radius = (width.coerceAtMost(height) / 2f) * 0.8f
                val path = android.graphics.Path()
                for (i in 0 until 6) {
                    val angle = Math.toRadians((i * 60 - 30).toDouble())
                    val x = centerX + (radius * cos(angle)).toFloat()
                    val y = centerY + (radius * sin(angle)).toFloat()
                    if (i == 0) {
                        path.moveTo(x, y)
                    } else {
                        path.lineTo(x, y)
                    }
                }
                path.close()
                canvas.drawPath(path, d20Paint)

                // D20 number
                canvas.drawText(
                    currentDieValue.toString(),
                    centerX,
                    centerY + (d20TextPaint.textSize / 3),
                    d20TextPaint,
                )
            }
        }
    }
}