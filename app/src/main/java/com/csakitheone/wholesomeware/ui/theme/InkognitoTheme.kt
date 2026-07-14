package com.csakitheone.wholesomeware.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont

private val RockSalt = FontFamily(
    Font(googleFont = GoogleFont("Rock Salt"), fontProvider = provider)
)

private val SpecialElite = FontFamily(
    Font(googleFont = GoogleFont("Special Elite"), fontProvider = provider)
)

private val BebasNeue = FontFamily(
    Font(googleFont = GoogleFont("Bebas Neue"), fontProvider = provider)
)

private val InkognitoColors = darkColorScheme(
    primary = Color(0xFFFFFFFF), // White
    onPrimary = Color(0xFF000000), // Black
    primaryContainer = Color(0xFF3D2B1F), // Dark Brown
    onPrimaryContainer = Color(0xFFFFFFFF),
    secondary = Color(0xFF4B3621), // Lighter Brown
    onSecondary = Color(0xFFE5E4E2), // Platinum
    background = Color(0xFF000000), // Black
    onBackground = Color(0xFFE5E4E2), // Platinum
    surface = Color(0xFF1A120B), // Very Dark Brown
    onSurface = Color(0xFFE5E4E2),
    surfaceVariant = Color(0xFF3D2B1F),
    onSurfaceVariant = Color(0xFFE5E4E2),
)

@Composable
fun InkognitoTheme(content: @Composable () -> Unit) {
    val typography = Typography(
        headlineLarge = Typography().headlineLarge.copy(fontFamily = RockSalt),
        headlineMedium = Typography().headlineMedium.copy(fontFamily = RockSalt),
        headlineSmall = Typography().headlineSmall.copy(fontFamily = RockSalt),
        titleLarge = Typography().titleLarge.copy(fontFamily = BebasNeue),
        titleMedium = Typography().titleMedium.copy(fontFamily = BebasNeue),
        titleSmall = Typography().titleSmall.copy(fontFamily = BebasNeue),
        bodyLarge = Typography().bodyLarge.copy(fontFamily = SpecialElite),
        bodyMedium = Typography().bodyMedium.copy(fontFamily = SpecialElite),
        bodySmall = Typography().bodySmall.copy(fontFamily = SpecialElite),
        labelLarge = Typography().labelLarge.copy(fontFamily = SpecialElite),
        labelMedium = Typography().labelMedium.copy(fontFamily = SpecialElite),
        labelSmall = Typography().labelSmall.copy(fontFamily = SpecialElite),
    )

    MaterialTheme(
        colorScheme = InkognitoColors,
        typography = typography,
        content = content
    )
}
