package com.csakitheone.wholesomeware.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.PorterDuff
import androidx.compose.runtime.LaunchedEffect
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.createBitmap
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import androidx.glance.currentState
import androidx.glance.layout.ContentScale
import androidx.glance.layout.fillMaxSize
import androidx.glance.state.GlanceStateDefinition
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.glance.BackgroundModifier
import androidx.glance.appwidget.SizeMode
import androidx.glance.background
import com.csakitheone.wholesomeware.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.math.min

class EmsiKovacsStickfigureClockWidgetReceiver : GlanceAppWidgetReceiver() {
    private var isActive = false

    override val glanceAppWidget: GlanceAppWidget = EmsiKovacsStickfigureClockWidget()

    override fun onEnabled(context: Context?) {
        super.onEnabled(context)
        isActive = true

        if (context == null) return

        GlobalScope.launch(Dispatchers.IO) {
            while (isActive) {
                val ids =
                    GlanceAppWidgetManager(context).getGlanceIds(EmsiKovacsStickfigureClockWidget::class.java)
                ids.forEach { id ->
                    updateAppWidgetState(context, id) {
                        it[longPreferencesKey("now")] = System.currentTimeMillis()
                    }
                }
                glanceAppWidget.updateAll(context)
                delay(1000L)
            }
        }
    }

    override fun onDisabled(context: Context?) {
        isActive = false
        super.onDisabled(context)
    }
}

class EmsiKovacsStickfigureClockWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Exact

    override val stateDefinition: GlanceStateDefinition<*>?
        get() = super.stateDefinition

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId
    ) {
        val bitmap = createBitmap(1080, 1080)
        val canvas = Canvas(bitmap)

        provideContent {
            val now = currentState<Preferences>()[longPreferencesKey("now")]

            LaunchedEffect(now) {
                drawClock(context, canvas)
            }

            Image(
                modifier = GlanceModifier.fillMaxSize()
                    .background(androidx.compose.ui.graphics.Color.White),
                provider = ImageProvider(bitmap),
                contentScale = ContentScale.Fit,
                contentDescription = now?.toString(),
            )
        }
    }

    private fun drawClock(context: Context, canvas: Canvas) {
        val centerX = canvas.width / 2f
        val centerY = canvas.height / 2f

        val calendar = Calendar.getInstance()
        val second = calendar.get(Calendar.SECOND)
        val minute = calendar.get(Calendar.MINUTE)
        val hour = calendar.get(Calendar.HOUR)

        // Calculate angles (0 degrees is at 12 o'clock position)
        val minuteAngle = ((minute - 15) * 6f + second * 0.1f)
        val hourAngle = ((hour - 6) * 30f + minute * 0.5f)

        // Clear canvas
        canvas.drawColor(Color.WHITE, PorterDuff.Mode.CLEAR)

        // Load the stick figure base image
        val baseBitmap = ResourcesCompat.getDrawable(
            context.resources,
            R.drawable.emsi_kovacs_stickfigure_legless,
            null
        )?.let { drawable ->
            val bitmap = createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight)
            val tempCanvas = Canvas(bitmap)
            drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
            drawable.draw(tempCanvas)
            bitmap
        }

        // Load the hour hand image
        val hourHandBitmap = ResourcesCompat.getDrawable(
            context.resources,
            R.drawable.emsi_kovacs_stickfigure_handle_hour,
            null
        )?.let { drawable ->
            val bitmap = createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight)
            val tempCanvas = Canvas(bitmap)
            drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
            drawable.draw(tempCanvas)
            bitmap
        }

        // Load the minute hand image
        val minuteHandBitmap = ResourcesCompat.getDrawable(
            context.resources,
            R.drawable.emsi_kovacs_stickfigure_handle_minute,
            null
        )?.let { drawable ->
            val bitmap = createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight)
            val tempCanvas = Canvas(bitmap)
            drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
            drawable.draw(tempCanvas)
            bitmap
        }

        // Draw the base stick figure
        baseBitmap?.let { base ->
            val scale = min(canvas.width, canvas.height).toFloat() / min(base.width, base.height)
            val scaledWidth = base.width * scale
            val scaledHeight = base.height * scale
            val left = centerX - scaledWidth / 2
            val top = centerY - scaledHeight / 2

            val matrix = Matrix().apply {
                postScale(scale, scale)
                postTranslate(left, top)
            }

            canvas.drawBitmap(base, matrix, null)
        }

        // Draw the hour hand (rotated)
        hourHandBitmap?.let { hand ->
            val scale = min(canvas.width, canvas.height).toFloat() / min(
                baseBitmap?.width ?: 1080,
                baseBitmap?.height ?: 1080
            )
            val matrix = Matrix().apply {
                postScale(scale, scale)
                postTranslate(-hand.width * scale / 2, -hand.height * scale / 2)
                postRotate(hourAngle, 0f, 0f)
                postTranslate(centerX, centerY)
            }

            canvas.drawBitmap(hand, matrix, null)
        }

        // Draw the minute hand (rotated)
        minuteHandBitmap?.let { hand ->
            val scale = min(canvas.width, canvas.height).toFloat() / min(
                baseBitmap?.width ?: 1080,
                baseBitmap?.height ?: 1080
            )
            val matrix = Matrix().apply {
                postScale(scale, scale)
                postTranslate(-hand.width * scale / 2, -hand.height * scale / 2)
                postRotate(minuteAngle, 0f, 0f)
                postTranslate(centerX, centerY)
            }

            canvas.drawBitmap(hand, matrix, null)
        }
    }
}


