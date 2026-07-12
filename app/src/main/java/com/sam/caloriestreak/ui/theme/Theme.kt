package com.sam.caloriestreak.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object CalorieStreakColors {
    val Success = Color(0xFF5FD28A)
    val Warning = Color(0xFFFFB86B)
    val Failure = Color(0xFFFF6B72)
    val Freeze = Color(0xFF77C7FF)
    val GraphScore = Color(0xFFA78BFA)
    val GraphCalories = Color(0xFF5BC0EB)
}

enum class AppThemeMode {
    DARK,
    LIGHT,
    SYSTEM
}

private val DarkColors = darkColorScheme(
    primary = Color(0xFFA78BFA),
    onPrimary = Color(0xFF160F2A),
    primaryContainer = Color(0xFF332653),
    onPrimaryContainer = Color(0xFFE9DDFF),
    secondary = Color(0xFF5FD28A),
    onSecondary = Color(0xFF062111),
    secondaryContainer = Color(0xFF173A25),
    onSecondaryContainer = Color(0xFFB9F5CB),
    tertiary = CalorieStreakColors.Freeze,
    onTertiary = Color(0xFF001E2B),
    tertiaryContainer = Color(0xFF14374A),
    onTertiaryContainer = Color(0xFFC8EBFF),
    error = CalorieStreakColors.Failure,
    onError = Color(0xFF2B0005),
    errorContainer = Color(0xFF50151B),
    onErrorContainer = Color(0xFFFFDADD),
    background = Color(0xFF0D0F12),
    onBackground = Color(0xFFF1F3F5),
    surface = Color(0xFF12151A),
    onSurface = Color(0xFFF1F3F5),
    surfaceVariant = Color(0xFF1B1F26),
    onSurfaceVariant = Color(0xFFBAC1CC),
    surfaceContainer = Color(0xFF171A20),
    surfaceContainerHigh = Color(0xFF20242C),
    outline = Color(0xFF737B88),
    outlineVariant = Color(0xFF343A44)
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF6650A4),
    secondary = Color(0xFF2E7D4A),
    tertiary = Color(0xFF27769B),
    error = Color(0xFFBA1A1A)
)

@Composable
fun CalorieStreakTheme(
    mode: AppThemeMode = AppThemeMode.DARK,
    content: @Composable () -> Unit
) {
    val useDark = when (mode) {
        AppThemeMode.DARK -> true
        AppThemeMode.LIGHT -> false
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    MaterialTheme(
        colorScheme = if (useDark) DarkColors else LightColors,
        content = content
    )
}