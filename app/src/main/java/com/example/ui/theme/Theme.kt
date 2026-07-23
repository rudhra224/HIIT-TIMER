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

fun parseHexColor(hex: String, fallback: Color = ElegantLavender): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: Exception) {
        fallback
    }
}

@Composable
fun HiitTimerTheme(
    themeMode: String = "DARK", // "SYSTEM", "DARK", "LIGHT"
    accentHex: String = "#D0BCFF",
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        "DARK" -> true
        "LIGHT" -> false
        else -> isSystemInDarkTheme()
    }

    val primaryColor = parseHexColor(accentHex, ElegantLavender)

    val darkColorScheme = darkColorScheme(
        primary = primaryColor,
        onPrimary = ElegantPurpleContainer,
        primaryContainer = ElegantPurpleContainer,
        onPrimaryContainer = ElegantOnPurpleContainer,
        secondary = NeonCyan,
        onSecondary = Color.Black,
        background = DarkBackground,
        onBackground = DarkOnSurface,
        surface = DarkSurface,
        onSurface = DarkOnSurface,
        surfaceVariant = DarkSurfaceVariant,
        onSurfaceVariant = DarkOnSurface.copy(alpha = 0.8f)
    )

    val lightColorScheme = lightColorScheme(
        primary = primaryColor,
        onPrimary = Color.White,
        primaryContainer = primaryColor.copy(alpha = 0.15f),
        onPrimaryContainer = primaryColor,
        secondary = NeonCyan,
        onSecondary = Color.Black,
        background = LightBackground,
        onBackground = LightOnSurface,
        surface = LightSurface,
        onSurface = LightOnSurface,
        surfaceVariant = LightSurfaceVariant,
        onSurfaceVariant = LightOnSurface.copy(alpha = 0.8f)
    )

    val colorScheme = if (darkTheme) darkColorScheme else lightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
