package com.sam.caloriestreak.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary = AppColors.Violet,
    onPrimary = Color(0xFF19112F),
    primaryContainer = Color(0xFF332A55),
    onPrimaryContainer = Color(0xFFEAE4FF),
    secondary = AppColors.Cyan,
    onSecondary = Color(0xFF002127),
    secondaryContainer = Color(0xFF123840),
    onSecondaryContainer = Color(0xFFC8F4FA),
    tertiary = AppColors.Coral,
    onTertiary = Color(0xFF35100A),
    tertiaryContainer = Color(0xFF4C2722),
    onTertiaryContainer = Color(0xFFFFDAD3),
    error = AppColors.Error,
    onError = Color(0xFF32000B),
    errorContainer = Color(0xFF531522),
    onErrorContainer = Color(0xFFFFD9DF),
    background = AppColors.Background,
    onBackground = AppColors.TextPrimary,
    surface = AppColors.Surface,
    onSurface = AppColors.TextPrimary,
    surfaceVariant = AppColors.SurfaceElevated,
    onSurfaceVariant = AppColors.TextSecondary,
    surfaceContainerLow = AppColors.Surface,
    surfaceContainer = AppColors.SurfaceElevated,
    surfaceContainerHigh = AppColors.SurfaceStrong,
    outline = Color(0xFF657084),
    outlineVariant = AppColors.Border,
    scrim = Color(0xE6000000)
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF6650A4),
    secondary = Color(0xFF006875),
    tertiary = Color(0xFF9A452F),
    error = Color(0xFFBA1A1A)
)

enum class AppThemeMode {
    DARK,
    LIGHT,
    SYSTEM
}

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
    CompositionLocalProvider(LocalAppSemanticColors provides DarkSemanticColors) {
        MaterialTheme(
            colorScheme = if (useDark) DarkColors else LightColors,
            typography = AppTypography,
            shapes = AppShapes,
            content = content
        )
    }
}

object AppTheme {
    val semanticColors: AppSemanticColors
        @Composable get() = LocalAppSemanticColors.current
}
