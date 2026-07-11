package com.sam.caloriestreak.ui.dashboard

import androidx.lifecycle.ViewModel
import com.sam.caloriestreak.domain.calculation.ScoreCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class DashboardUiState(
    val loading: Boolean = false,
    val caloriesToday: Double = 0.0,
    val targetCalories: Double = 1650.0,
    val score: Double = 0.0,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val storedFreezes: Int = 0,
    val error: String? = null
)

class DashboardViewModel(
    private val scoreCalculator: ScoreCalculator = ScoreCalculator()
) : ViewModel() {
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    fun previewCalories(calories: Double) {
        _uiState.value = _uiState.value.copy(
            caloriesToday = calories,
            score = scoreCalculator.calculate(calories)
        )
    }
}
