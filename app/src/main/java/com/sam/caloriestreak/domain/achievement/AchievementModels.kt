package com.sam.caloriestreak.domain.achievement

enum class AchievementCategory {
    TIME,
    CALORIES,
    SCORE,
    RECIPES,
    MEAL_HABITS,
    STREAKS,
    FREEZES,
    WEIGHT,
    PROTEIN,
    GROCERY,
    HIDDEN
}

data class AchievementDefinition(
    val id: String,
    val title: String,
    val description: String,
    val category: AchievementCategory,
    val iconKey: String? = null,
    val hidden: Boolean = false,
    val repeatable: Boolean = false,
    val threshold: Double? = null,
    val sortOrder: Int
)

data class ConsecutiveRecordingAchievementDefinition(
    val id: String,
    val title: String,
    val requiredDays: Int,
    val unlockComment: String,
    val iconKey: String? = "calendar",
    val sortOrder: Int
) {
    fun asAchievement() = AchievementDefinition(
        id = id,
        title = title,
        description = unlockComment,
        category = AchievementCategory.TIME,
        iconKey = iconKey,
        threshold = requiredDays.toDouble(),
        sortOrder = sortOrder
    )
}
