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
import android.view.MotionEvent
import android.view.SurfaceHolder
import java.util.Calendar
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import androidx.core.graphics.toColorInt
import kotlin.apply
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.max

class KoloraFesztAnalogClockWallpaperService : WallpaperService() {
    override fun onCreateEngine(): Engine {
        return object : Engine() {
            private lateinit var powerManager: PowerManager
            private val handler = Handler(Looper.getMainLooper())
            private var visible = false
            private var width = 0
            private var height = 0
            private var centerX = 0f
            private var centerY = 0f
            private var isTouching = false
            private var prevTouchX = 0f
            private var prevTouchY = 0f
            private var prevRadian = 0f
            private var holdingCookie: Int? = null
            private var secondRadius = 0f
            private var minuteRadius = 0f
            private var hourRadius = 0f
            private var secondTouchModifier = 1f
            private var minuteTouchModifier = 1f
            private var hourTouchModifier = 1f
            private var secondRotation = 0f
            private var minuteRotation = 0f
            private var hourRotation = 0f

            private fun String.toPaint(): Paint = Paint().apply {
                color = this@toPaint.toColorInt()
                style = Paint.Style.FILL
                isAntiAlias = true
            }

            private val drawRunnable = object : Runnable {
                override fun run() {
                    val framerate = if (powerManager.isPowerSaveMode) 1 else 60
                    draw(1000L / framerate)
                    if (visible) {
                        handler.postDelayed(this, 1000L / framerate)
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

            override fun onTouchEvent(event: MotionEvent?) {
                fun isInside(x: Float, y: Float, radius: Float): Boolean {
                    return (x - centerX) * (x - centerX) + (y - centerY) * (y - centerY) <= radius * radius
                }

                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        isTouching = !powerManager.isPowerSaveMode
                        holdingCookie = when {
                            isInside(event.x, event.y, hourRadius) -> 3
                            isInside(event.x, event.y, minuteRadius) -> 2
                            isInside(event.x, event.y, secondRadius) -> 1
                            else -> null
                        }
                        prevRadian = atan2(event.y - centerY, event.x - centerX)
                        prevTouchX = event.x
                        prevTouchY = event.y
                    }

                    MotionEvent.ACTION_MOVE -> {
                        val currentRadian = atan2(event.y - centerY, event.x - centerX)
                        when (holdingCookie) {
                            3 -> {
                                hourRotation += currentRadian - prevRadian
                            }
                            2 -> {
                                minuteRotation += currentRadian - prevRadian
                            }
                            1 -> {
                                secondRotation += currentRadian - prevRadian
                            }
                        }
                        prevRadian = currentRadian
                        prevTouchX = event.x
                        prevTouchY = event.y
                    }

                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        isTouching = false
                        holdingCookie = null
                    }
                }
                super.onTouchEvent(event)
            }

            private fun draw(deltaTime: Long) {
                val holder = surfaceHolder
                var canvas: Canvas? = null
                try {
                    canvas = holder.lockCanvas()
                    if (canvas != null) {
                        drawClock(canvas, deltaTime)
                    }
                } finally {
                    if (canvas != null) {
                        holder.unlockCanvasAndPost(canvas)
                    }
                }
            }

            private fun drawClock(canvas: Canvas, deltaTime: Long) {
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

                canvas.drawRect(
                    0f,
                    0f,
                    width.toFloat(),
                    height.toFloat(),
                    backgroundColor.toPaint()
                )

                centerX = width / 2f
                centerY = height / 2f

                hourTouchModifier = minuteTouchModifier
                minuteTouchModifier = secondTouchModifier
                secondTouchModifier = if (isTouching) {
                    max(.95f, secondTouchModifier - .0005f * deltaTime)
                } else {
                    min(1f, secondTouchModifier + .0005f * deltaTime)
                }

                secondRadius = min(width, height) * .65f * secondTouchModifier
                minuteRadius = min(width, height) * .45f * minuteTouchModifier
                hourRadius = min(width, height) * .25f * hourTouchModifier

                val calendar = Calendar.getInstance()
                val millisecond = calendar.get(Calendar.MILLISECOND)
                val second = calendar.get(Calendar.SECOND)
                val minute = calendar.get(Calendar.MINUTE)
                val hour = calendar.get(Calendar.HOUR)

                secondRotation = if (holdingCookie == 1) secondRotation else
                    Math.toRadians(second * 6.0 + (millisecond / 1000.0) * 6.0 - 90).toFloat()
                minuteRotation = if (holdingCookie == 2) minuteRotation else
                    Math.toRadians(minute * 6.0 + (second / 60.0) * 6.0 - 90).toFloat()
                hourRotation = if (holdingCookie == 3) hourRotation else
                    Math.toRadians(hour * 30.0 - 90).toFloat()

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

                canvas.drawPath(
                    wavyCirclePath(hourRadius, hourRotation),
                    hourSurfaceColor.toPaint()
                )
                canvas.drawCircle(
                    centerX + (hourRadius * .94f) * cos(hourRotation),
                    centerY + (hourRadius * .94f) * sin(hourRotation),
                    hourRadius * .06f,
                    hourDotColor.toPaint()
                )
            }
        }
    }
}

