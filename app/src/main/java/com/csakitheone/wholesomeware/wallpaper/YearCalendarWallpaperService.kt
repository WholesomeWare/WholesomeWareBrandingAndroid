package com.csakitheone.wholesomeware.wallpaper

import android.app.KeyguardManager
import android.app.WallpaperColors
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.service.wallpaper.WallpaperService
import android.view.MotionEvent
import android.view.SurfaceHolder
import androidx.annotation.RequiresApi
import androidx.core.graphics.toColorInt
import java.util.Calendar
import kotlin.math.min

class YearCalendarWallpaperService : WallpaperService() {
    override fun onCreateEngine(): Engine {
        return object : Engine() {
            private val keyguardManager by lazy {
                getSystemService(KeyguardManager::class.java)
            }
            private val handler = Handler(Looper.getMainLooper())
            private var visible = false
            private var width = 0
            private var height = 0
            private var isTouching = false

            // Calendar data
            private val calendar = Calendar.getInstance()
            private var currentDayOfYear = 0
            private var totalDaysInYear = 0

            /**
             * Convert a hex color string to a Paint object
             */
            private fun String.toPaint(): Paint = Paint().apply {
                color = this@toPaint.toColorInt()
                style = Paint.Style.FILL
                isAntiAlias = true
            }

            /**
             * Update calendar information
             */
            private fun updateCalendarData() {
                calendar.timeInMillis = System.currentTimeMillis()
                currentDayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
                totalDaysInYear = if (isLeapYear(calendar.get(Calendar.YEAR))) 366 else 365
            }

            /**
             * Check if a year is a leap year
             */
            private fun isLeapYear(year: Int): Boolean {
                return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
            }

            private val drawRunnable = object : Runnable {
                override fun run() {
                    // Update calendar data once per draw cycle
                    updateCalendarData()

                    // Adjust framerate based on power saving mode
                    val framerate = 15
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
                updateCalendarData()
            }

            override fun onVisibilityChanged(visible: Boolean) {
                this.visible = visible
                if (visible) {
                    updateCalendarData()
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

                val backgroundColor = if (isDarkMode) "#0D1117" else "#A0A0A0"
                val foregroundColor = "#f06a3e"

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
             * Draw the year calendar on canvas
             */
            private fun drawCanvas(canvas: Canvas, deltaTime: Long) {
                val isDarkMode = (resources.configuration.uiMode and
                        Configuration.UI_MODE_NIGHT_MASK) ==
                        Configuration.UI_MODE_NIGHT_YES

                // Define colors based on theme
                val backgroundColor = if (isDarkMode) "#0D1117" else "#A0A0A0"
                val completedColor = if (isDarkMode) "#CACACA" else "#FFFFFF"
                val remainingColor = if (isDarkMode) "#30363D" else "#CACACA"
                val todayColor = "#f06a3e"
                val textColor = if (isDarkMode) "#E6EDF3" else "#212121"

                // Clear background
                canvas.drawRect(
                    0f,
                    0f,
                    width.toFloat(),
                    height.toFloat(),
                    backgroundColor.toPaint()
                )

                // Calculate layout
                val padding = 92f
                val availableWidth = width - (padding * 2)
                val availableHeight = height - (padding * 2)

                // Calculate grid dimensions (trying to fit dots in a grid)
                val dotsPerRow = 15 // 20 dots per row
                val totalRows = (totalDaysInYear + dotsPerRow - 1) / dotsPerRow // Ceiling division

                // Calculate dot size
                val dotSpacing = 16f
                val maxDotSize = min(
                    (availableWidth - (dotsPerRow - 1) * dotSpacing) / dotsPerRow,
                    (availableHeight - (totalRows - 1) * dotSpacing) / totalRows
                )
                val dotRadius = maxDotSize / 2f

                // Center the grid
                val gridWidth = dotsPerRow * maxDotSize + (dotsPerRow - 1) * dotSpacing
                val gridHeight = totalRows * maxDotSize + (totalRows - 1) * dotSpacing
                val startX = padding + (availableWidth - gridWidth) / 2f
                val startY = padding + (availableHeight - gridHeight) / 2f

                // Draw progress text
                if (keyguardManager.isKeyguardLocked) {
                    val progressPaint = Paint().apply {
                        color = textColor.toColorInt()
                        textSize = 32f
                        isAntiAlias = true
                        textAlign = Paint.Align.CENTER
                    }
                    val progressPercentage = (currentDayOfYear * 100f / totalDaysInYear).toInt()
                    canvas.drawText(
                        "Day $currentDayOfYear of $totalDaysInYear ($progressPercentage%)",
                        width / 2f,
                        startY + gridHeight + 60f,
                        progressPaint
                    )
                }

                // Draw dots for each day
                val completedPaint = completedColor.toPaint()
                val remainingPaint = remainingColor.toPaint()
                val todayPaint = todayColor.toPaint()

                for (day in 1..totalDaysInYear) {
                    val row = (day - 1) / dotsPerRow
                    val col = (day - 1) % dotsPerRow

                    val x = startX + col * (maxDotSize + dotSpacing) + dotRadius
                    val y = startY + row * (maxDotSize + dotSpacing) + dotRadius

                    // Determine which color to use
                    val paint = when {
                        day == currentDayOfYear -> todayPaint
                        day < currentDayOfYear -> completedPaint
                        else -> remainingPaint
                    }

                    // Add pulsing effect on current day if touching
                    val radius = if (day == currentDayOfYear && isTouching) {
                        dotRadius * 1.3f
                    } else {
                        dotRadius
                    }

                    canvas.drawCircle(x, y, radius, paint)
                }
            }
        }
    }
}

