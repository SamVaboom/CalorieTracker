package com.sam.caloriestreak.ui.meal_log

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sam.caloriestreak.data.local.entity.MealLogEntity
import com.sam.caloriestreak.domain.search.SearchMatcher
import com.sam.caloriestreak.ui.components.AppEmptyState
import com.sam.caloriestreak.ui.components.AppSearchField
import com.sam.caloriestreak.ui.components.AppSectionHeader
import com.sam.caloriestreak.ui.components.MealLogRow
import com.sam.caloriestreak.ui.theme.AppDimensions
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun ProteinCorrectionScreen(
    meals: List<MealLogEntity>,
    onDelete: (MealLogEntity) -> Unit,
    onRecalculateRecipeProtein: (MealLogEntity) -> Result<Unit>,
    onSetManualProtein: (MealLogEntity, Double?) -> Result<Unit>
) {
    var query by remember { mutableStateOf("") }
    val filtered = remember(meals, query) {
        meals.filter { meal ->
            SearchMatcher.matches(query, meal.recipeName, meal.portionDescription, meal.note)
        }.sortedWith(compareByDescending<MealLogEntity> { it.dateEpochDay }.thenByDescending { it.timeMillis })
    }
    val grouped = remember(filtered) { filtered.groupBy { it.dateEpochDay }.toList().sortedByDescending { it.first } }

    LazyColumn(
        contentPadding = PaddingValues(
            start = AppDimensions.ScreenPadding,
            top = AppDimensions.Space16,
            end = AppDimensions.ScreenPadding,
            bottom = 96.dp
        ),
        verticalArrangement = Arrangement.spacedBy(AppDimensions.Space12)
    ) {
        item {
            AppSectionHeader(
                title = "Protein Corrections",
                subtitle = "Explicitly update historical protein without changing stored calories"
            )
        }
        item { AppSearchField(query, { query = it }, "Search logged meals") }
        item {
            Text(
                "Recipe meals can be recalculated from the current recipe. Manual meals accept a protein value or may be returned to unknown. Nothing changes until you confirm an edit.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (filtered.isEmpty()) {
            item {
                AppEmptyState(
                    icon = Icons.Outlined.EditNote,
                    title = if (query.isBlank()) "No logged meals" else "No matching meals",
                    message = if (query.isBlank()) "Meals will appear here after food is logged." else "Try another search term."
                )
            }
        }
        grouped.forEach { (day, dayMeals) ->
            item(key = "correction-day-$day") {
                Text(
                    LocalDate.ofEpochDay(day).format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy")),
                    style = MaterialTheme.typography.titleMedium
                )
            }
            items(dayMeals, key = { "correction-${it.id}" }) { meal ->
                MealLogRow(
                    meal = meal,
                    onDelete = onDelete,
                    onRecalculateProtein = onRecalculateRecipeProtein,
                    onSetManualProtein = onSetManualProtein
                )
            }
        }
    }
}
