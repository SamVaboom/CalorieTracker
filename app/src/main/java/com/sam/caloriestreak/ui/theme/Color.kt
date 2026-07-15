package com.sam.caloriestreak.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

object AppColors {
    val Background = Color(0xFF0B0E14)
    val Surface = Color(0xFF121722)
    val SurfaceElevated = Color(0xFF181E2B)
    val SurfaceStrong = Color(0xFF202738)
    val Border = Color(0xFF2A3345)

    val TextPrimary = Color(0xFFF4F6FB)
    val TextSecondary = Color(0xFFB3BCCB)
    val TextMuted = Color(0xFF7F899A)

    val Violet = Color(0xFFA997FF)
    val Cyan = Color(0xFF55D6E8)
    val Coral = Color(0xFFFF8A78)
    val Success = Color(0xFF65D98B)
    val Warning = Color(0xFFF3B85F)
    val Error = Color(0xFFFF6F83)
    val Freeze = Color(0xFF78CCFF)
    val Weight = Color(0xFF5FD4B4)
    val Achievement = Color(0xFFFFD166)
    val Locked = Color(0xFF657083)
}

@Immutable
data class AppSemanticColors(
    val success: Color,
    val onSuccess: Color,
    val successContainer: Color,
    val warning: Color,
    val onWarning: Color,
    val freeze: Color,
    val weight: Color,
    val achievementEarned: Color,
    val achievementLocked: Color
)

val DarkSemanticColors = AppSemanticColors(
    success = AppColors.Success,
    onSuccess = Color(0xFF062414),
    successContainer = Color(0xFF163D27),
    warning = AppColors.Warning,
    onWarning = Color(0xFF2C1B00),
    freeze = AppColors.Freeze,
    weight = AppColors.Weight,
    achievementEarned = AppColors.Achievement,
    achievementLocked = AppColors.Locked
)

val LocalAppSemanticColors = staticCompositionLocalOf { DarkSemanticColors }

object CalorieStreakColors {
    val Success = AppColors.Success
    val Warning = AppColors.Warning
    val Failure = AppColors.Error
    val Freeze = AppColors.Freeze
    val GraphScore = AppColors.Violet
    val GraphCalories = AppColors.Coral
    val GraphWeight = AppColors.Weight
}
