package com.csakitheone.wholesomeware.wallpaper

import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import java.util.Calendar
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import androidx.core.graphics.toColorInt
import kotlin.apply
import kotlin.math.PI

class KoloraFesztAnalogClockWallpaperService : WallpaperService() {
    override fun onCreateEngine(): Engine {
        return AnalogClockEngine()
    }

    inner class AnalogClockEngine : Engine() {
        private lateinit var powerManager: PowerManager
        private val handler = Handler(Looper.getMainLooper())
        private var visible = false
        private var width = 0
        private var height = 0

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
                    handler.postDelayed(this, 1000 / framerate.toLong())
                }
            }
        }

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
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder?) {
            super.onSurfaceDestroyed(holder)
            visible = false
            handler.removeCallbacks(drawRunnable)
        }

        private fun draw() {
            val holder = surfaceHolder
            var canvas: Canvas? = null
            try {
                canvas = holder.lockCanvas()
                if (canvas != null) {
                    drawClock(canvas)
                }
            } finally {
                if (canvas != null) {
                    holder.unlockCanvasAndPost(canvas)
                }
            }
        }

        private fun drawClock(canvas: Canvas) {
            val isDarkMode = (resources.configuration.uiMode and
                    Configuration.UI_MODE_NIGHT_MASK) ==
                    Configuration.UI_MODE_NIGHT_YES

            val darkBlue = "#33214b"
            val brown = "#8d552e"
            val beige = "#d5c3b6"
            val backgroundColor = if (isDarkMode) darkBlue else beige
            val secondSurfaceColor = if (isDarkMode) brown else darkBlue
            val secondDotColor = beige
            val minuteSurfaceColor = if (isDarkMode) beige else brown
            val minuteDotColor = if (isDarkMode) darkBlue else beige
            val hourSurfaceColor = if (isDarkMode) darkBlue else beige
            val hourDotColor = if (isDarkMode) beige else brown

            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), backgroundColor.toPaint())

            val centerX = width / 2f
            val centerY = height / 2f
            val secondRadius = min(width, height) * .7f
            val minuteRadius = min(width, height) * .5f
            val hourRadius = min(width, height) * .3f

            val calendar = Calendar.getInstance()
            val millisecond = calendar.get(Calendar.MILLISECOND)
            val second = calendar.get(Calendar.SECOND)
            val minute = calendar.get(Calendar.MINUTE)
            val hour = calendar.get(Calendar.HOUR)
            val secondRotation =
                Math.toRadians(second * 6.0 + (millisecond / 1000.0) * 6.0 - 90).toFloat()
            val minuteRotation = Math.toRadians(minute * 6.0 + (second / 60.0) * 6.0 - 90).toFloat()
            val hourRotation = Math.toRadians(hour * 30.0 - 90).toFloat()

            fun wavyCirclePath(radius: Float, rotation: Float = 0f): Path = Path().apply {
                moveTo(centerX, centerY - radius)
                for (i in 0..360) {
                    val angle = Math.toRadians(i.toDouble())
                    val waveRadius = radius + radius * .05f * sin(16 * angle).toFloat()
                    val x = centerX + waveRadius * cos(angle + rotation - PI / 32).toFloat()
                    val y = centerY + waveRadius * sin(angle + rotation - PI / 32).toFloat()
                    lineTo(x, y)
                }
                close()
            }

            if (!powerManager.isPowerSaveMode) {
                canvas.drawPath(
                    wavyCirclePath(secondRadius, secondRotation),
                    secondSurfaceColor.toPaint()
                )
                canvas.drawCircle(
                    centerX + (secondRadius * .9f) * cos(secondRotation),
                    centerY + (secondRadius * .9f) * sin(secondRotation),
                    secondRadius * .07f,
                    secondDotColor.toPaint()
                )
            }

            canvas.drawPath(
                wavyCirclePath(minuteRadius, minuteRotation),
                minuteSurfaceColor.toPaint()
            )
            canvas.drawCircle(
                centerX + (minuteRadius * .92f) * cos(minuteRotation),
                centerY + (minuteRadius * .92f) * sin(minuteRotation),
                minuteRadius * .07f,
                minuteDotColor.toPaint()
            )

            canvas.drawPath(wavyCirclePath(hourRadius, hourRotation), hourSurfaceColor.toPaint())
            canvas.drawCircle(
                centerX + (hourRadius * .94f) * cos(hourRotation),
                centerY + (hourRadius * .94f) * sin(hourRotation),
                hourRadius * .06f,
                hourDotColor.toPaint()
            )
        }
    }
}

