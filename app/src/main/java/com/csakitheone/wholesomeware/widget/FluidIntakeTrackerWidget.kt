package com.csakitheone.wholesomeware.widget

import android.content.Context
import android.graphics.drawable.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.IconImageProvider
import androidx.glance.Image
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.csakitheone.wholesomeware.R
import java.time.LocalDate

private val FLUID_GOAL_ML_KEY = intPreferencesKey("fluid_goal_ml")
private val AMOUNT_PARAM = ActionParameters.Key<Int>("amount")

private fun getFluidIntakeKeyForToday(): Preferences.Key<Int> {
    val today = LocalDate.now().toString()
    return intPreferencesKey("fluid_intake_ml_$today")
}

class FluidIntakeTrackerWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = FluidIntakeTrackerWidget()

    override fun onUpdate(
        context: Context,
        appWidgetManager: android.appwidget.AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
    }
}

class FluidIntakeTrackerWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Responsive(
        (2..4).flatMap { w ->
            (2..3).map { h ->
                DpSize(
                    width = (w * 100).dp,
                    height = (h * 100).dp,
                )
            }
        }.toSet()
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme {
                FluidIntakeContent()
            }
        }
    }

    @Composable
    private fun FluidIntakeContent() {
        val size = LocalSize.current

        val prefs = currentState<Preferences>()
        val currentIntake = prefs[getFluidIntakeKeyForToday()] ?: 0
        val dailyGoal = prefs[FLUID_GOAL_ML_KEY] ?: 2000 // Default 2L goal
        val percentage = (currentIntake.toFloat() / dailyGoal * 100).toInt()

        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.background),
            contentAlignment = Alignment.BottomCenter,
        ) {
            Box(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .height(size.height * (percentage.coerceIn(0, 100) / 100f))
                    .background(GlanceTheme.colors.primaryContainer),
            ) {}

            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "💧 Folyadékbevitel követő",
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = GlanceTheme.colors.onBackground,
                    ),
                    maxLines = 1,
                )

                Spacer(modifier = GlanceModifier.size(8.dp))

                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = GlanceModifier.fillMaxHeight().defaultWeight(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "$percentage%",
                            style = TextStyle(
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = GlanceTheme.colors.onBackground,
                            )
                        )
                        Spacer(modifier = GlanceModifier.size(8.dp))
                        Text(
                            text = "Cél: ${dailyGoal}ml",
                            style = TextStyle(
                                color = GlanceTheme.colors.onBackground,
                            )
                        )
                    }
                    Column(
                        modifier = GlanceModifier.fillMaxHeight().width(42.dp),
                        horizontalAlignment = Alignment.End,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        AddButton(R.drawable.ic_glass_pint, 50)
                        Spacer(modifier = GlanceModifier.size(8.dp))
                        AddButton(R.drawable.ic_cup, 200)
                        if (size.height >= 250.dp)  {
                            Spacer(modifier = GlanceModifier.size(8.dp))
                            AddButton(R.drawable.ic_bottle_soda, 500)
                            Spacer(modifier = GlanceModifier.size(8.dp))
                            Box(
                                modifier = GlanceModifier
                                    .background(GlanceTheme.colors.secondary)
                                    .size(42.dp)
                                    .cornerRadius(21.dp)
                                    .clickable(actionRunCallback<ResetIntakeAction>()),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    provider = IconImageProvider(
                                        Icon.createWithResource(
                                            LocalContext.current,
                                            R.drawable.ic_refresh,
                                        )
                                    ),
                                    contentDescription = null,
                                )
                            }
                        }
                    }
                    if (size.height < 250.dp) {
                        Spacer(modifier = GlanceModifier.size(8.dp))
                        Column(
                            modifier = GlanceModifier.fillMaxHeight().width(42.dp),
                            horizontalAlignment = Alignment.End,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            AddButton(R.drawable.ic_bottle_soda, 500)
                            Spacer(modifier = GlanceModifier.size(8.dp))
                            Box(
                                modifier = GlanceModifier
                                    .background(GlanceTheme.colors.secondary)
                                    .size(42.dp)
                                    .cornerRadius(21.dp)
                                    .clickable(actionRunCallback<ResetIntakeAction>()),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    provider = IconImageProvider(
                                        Icon.createWithResource(
                                            LocalContext.current,
                                            R.drawable.ic_refresh,
                                        )
                                    ),
                                    contentDescription = null,
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun AddButton(iconResource: Int, amount: Int) {
        Column(
            modifier = GlanceModifier
                .background(GlanceTheme.colors.primary)
                .size(42.dp)
                .cornerRadius(8.dp)
                .clickable(
                    actionRunCallback<AddFluidAction>(
                        actionParametersOf(AMOUNT_PARAM to amount)
                    )
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                modifier = GlanceModifier.size(16.dp),
                provider = IconImageProvider(
                    Icon.createWithResource(
                        LocalContext.current,
                        iconResource
                    )
                ),
                contentDescription = null,
            )
            Text(
                text = "+${amount}ml",
                style = TextStyle(
                    fontSize = 8.sp,
                    color = GlanceTheme.colors.onPrimary,
                )
            )
        }
    }
}

// Action callbacks
class AddFluidAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val amount = parameters[AMOUNT_PARAM] ?: 0
        updateAppWidgetState(context, glanceId) { prefs ->
            val currentIntake = prefs[getFluidIntakeKeyForToday()] ?: 0
            prefs[getFluidIntakeKeyForToday()] = currentIntake + amount
        }
        FluidIntakeTrackerWidget().update(context, glanceId)
    }
}

class ResetIntakeAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        updateAppWidgetState(context, glanceId) { prefs ->
            prefs[getFluidIntakeKeyForToday()] = 0
        }
        FluidIntakeTrackerWidget().update(context, glanceId)
    }
}