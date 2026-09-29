package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = AgriGreenLight,
    onPrimary = Color(0xFF003822),
    primaryContainer = AgriGreenContainerDark,
    onPrimaryContainer = Color(0xFFA7F3D0),

    secondary = WaterBlueLight,
    onSecondary = Color(0xFF003548),
    secondaryContainer = WaterBlueContainerDark,
    onSecondaryContainer = Color(0xFFBAE6FD),

    tertiary = SoilAmberLight,
    onTertiary = Color(0xFF452B00),
    tertiaryContainer = Color(0xFF332005),
    onTertiaryContainer = Color(0xFFFDE68A),

    background = DarkBg,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkBorder,
    error = StatusAlert,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = AgriGreenDark,
    onPrimary = Color.White,
    primaryContainer = AgriGreenContainerLight,
    onPrimaryContainer = Color(0xFF065F46),

    secondary = WaterBlueDark,
    onSecondary = Color.White,
    secondaryContainer = WaterBlueContainerLight,
    onSecondaryContainer = Color(0xFF0369A1),

    tertiary = SoilAmberDark,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFEF3C7),
    onTertiaryContainer = Color(0xFF92400E),

    background = LightBg,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary,
    outline = LightBorder,
    error = StatusAlert,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to sleek tech Dark theme for smart agritech demo
    dynamicColor: Boolean = false, // Keep intentional agricultural brand colors consistent
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
