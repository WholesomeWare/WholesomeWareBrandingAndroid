package com.csakitheone.wholesomeware.widget

import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxSize
import androidx.glance.text.Text
import androidx.core.graphics.createBitmap
import androidx.core.graphics.toColorInt
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.layout.ContentScale
import java.util.Calendar
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class KoloraFesztAnalogClockWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = KoloraFesztAnalogClockWidget()
}

class KoloraFesztAnalogClockWidget : GlanceAppWidget() {
    override suspend fun provideGlance(
        context: Context,
        id: GlanceId
    ) {
        val bitmap = createBitmap(1080, 1080)
        val canvas = Canvas(bitmap)

        drawClock(context, canvas)

        provideContent {
            Image(
                modifier = GlanceModifier.fillMaxSize(),
                provider = ImageProvider(bitmap),
                contentScale = ContentScale.Fit,
                contentDescription = null,
            )
        }
    }

    private fun String.toPaint(): Paint = Paint().apply {
        color = this@toPaint.toColorInt()
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private fun drawClock(context: Context, canvas: Canvas) {
        val isDarkMode = (context.resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

        val darkBlue = "#33214b"
        val brown = "#8d552e"
        val beige = "#d5c3b6"
        val secondSurfaceColor = if (isDarkMode) brown else darkBlue
        val secondDotColor = beige
        val minuteSurfaceColor = if (isDarkMode) beige else brown
        val minuteDotColor = if (isDarkMode) darkBlue else beige
        val hourSurfaceColor = if (isDarkMode) darkBlue else beige
        val hourDotColor = if (isDarkMode) beige else brown

        val centerX = canvas.width / 2f
        val centerY = canvas.height / 2f

        val secondRadius = min(canvas.width, canvas.height) / 2 * .9f
        val minuteRadius = min(canvas.width, canvas.height) / 2 * .6f
        val hourRadius = min(canvas.width, canvas.height) / 2 * .35f

        val calendar = Calendar.getInstance()
        val millisecond = calendar.get(Calendar.MILLISECOND)
        val second = calendar.get(Calendar.SECOND)
        val minute = calendar.get(Calendar.MINUTE)
        val hour = calendar.get(Calendar.HOUR)

        val secondRotation =
            Math.toRadians(second * 6.0 + (millisecond / 1000.0) * 6.0 - 90).toFloat()
        val minuteRotation =
            Math.toRadians(minute * 6.0 + (second / 60.0) * 6.0 - 90).toFloat()
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

        canvas.drawPath(
            wavyCirclePath(secondRadius, secondRotation),
            secondSurfaceColor.toPaint()
        )
        canvas.drawCircle(
            centerX + (secondRadius * .88f) * cos(secondRotation),
            centerY + (secondRadius * .88f) * sin(secondRotation),
            secondRadius * .08f,
            secondDotColor.toPaint()
        )

        canvas.drawPath(
            wavyCirclePath(minuteRadius, minuteRotation),
            minuteSurfaceColor.toPaint()
        )
        canvas.drawCircle(
            centerX + (minuteRadius * .88f) * cos(minuteRotation),
            centerY + (minuteRadius * .88f) * sin(minuteRotation),
            minuteRadius * .08f,
            minuteDotColor.toPaint()
        )

        canvas.drawPath(
            wavyCirclePath(hourRadius, hourRotation),
            hourSurfaceColor.toPaint()
        )
        canvas.drawCircle(
            centerX + (hourRadius * .86f) * cos(hourRotation),
            centerY + (hourRadius * .86f) * sin(hourRotation),
            hourRadius * .1f,
            hourDotColor.toPaint()
        )
    }

}