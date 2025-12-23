package com.csakitheone.wholesomeware.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
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
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.csakitheone.wholesomeware.experiment.DartsHelper

private val SCORE_KEY = stringPreferencesKey("current_score")
private val NUMBER_PARAM = ActionParameters.Key<Int>("number")

class DartsHelperWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = DartsHelperWidget()
}

class DartsHelperWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme {
                DartsHelperContent()
            }
        }
    }

    @Composable
    private fun DartsHelperContent() {
        val prefs = currentState<Preferences>()
        val currentScoreText = prefs[SCORE_KEY] ?: ""

        val currentScore = currentScoreText.toIntOrNull() ?: 0
        val throws = if (currentScore > 0) {
            DartsHelper.getLeastThrowsToWin(currentScore)
        } else {
            emptyList()
        }

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.background)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Header
            Text(
                modifier = GlanceModifier.height(24.dp),
                text = "Darts: legkevesebb dobás számláló",
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    color = GlanceTheme.colors.onBackground,
                )
            )

            Spacer(modifier = GlanceModifier.size(4.dp))

            // Score display
            Box(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .background(GlanceTheme.colors.secondaryContainer)
                    .cornerRadius(8.dp)
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (currentScoreText.isEmpty()) "Írj be egy pontszámot"
                    else "Pont: $currentScoreText\n${throws.size} dobás kell: " +
                            throws.joinToString(),
                    style = TextStyle(
                        color = GlanceTheme.colors.onSecondaryContainer,
                    )
                )
            }

            Spacer(modifier = GlanceModifier.size(4.dp))

            Column(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    NumberButton(0)
                    NumberButton(1)
                    NumberButton(2)
                    NumberButton(3)
                    NumberButton(4)
                    NumberButton(5)
                }

                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    NumberButton(6)
                    NumberButton(7)
                    NumberButton(8)
                    NumberButton(9)
                    ActionButton("C", actionRunCallback<ClearAction>())
                    ActionButton("⌫", actionRunCallback<BackspaceAction>())
                }
            }
        }
    }

    @Composable
    private fun NumberButton(number: Int) {
        Box(
            modifier = GlanceModifier
                .size(40.dp)
                .background(GlanceTheme.colors.primaryContainer)
                .cornerRadius(8.dp)
                .clickable(
                    actionRunCallback<NumberInputAction>(
                        actionParametersOf(NUMBER_PARAM to number)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number.toString(),
                style = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = GlanceTheme.colors.onPrimaryContainer
                )
            )
        }
    }

    @Composable
    private fun ActionButton(label: String, action: androidx.glance.action.Action) {
        Box(
            modifier = GlanceModifier
                .size(40.dp)
                .background(GlanceTheme.colors.secondaryContainer)
                .cornerRadius(8.dp)
                .clickable(action),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = GlanceTheme.colors.onSecondaryContainer
                )
            )
        }
    }
}

// Action callbacks
class NumberInputAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val number = parameters[NUMBER_PARAM] ?: return
        updateAppWidgetState(context, glanceId) { prefs ->
            val currentScore = prefs[SCORE_KEY] ?: ""
            val newScore = if (currentScore.length < 4) {
                currentScore + number.toString()
            } else {
                currentScore
            }
            prefs[SCORE_KEY] = newScore
        }
        DartsHelperWidget().update(context, glanceId)
    }
}

class BackspaceAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        updateAppWidgetState(context, glanceId) { prefs ->
            val currentScore = prefs[SCORE_KEY] ?: ""
            prefs[SCORE_KEY] = if (currentScore.isNotEmpty()) {
                currentScore.dropLast(1)
            } else {
                ""
            }
        }
        DartsHelperWidget().update(context, glanceId)
    }
}

class ClearAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        updateAppWidgetState(context, glanceId) { prefs ->
            prefs[SCORE_KEY] = ""
        }
        DartsHelperWidget().update(context, glanceId)
    }
}
