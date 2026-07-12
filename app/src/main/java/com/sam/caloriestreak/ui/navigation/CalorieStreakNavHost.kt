package com.sam.caloriestreak.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sam.caloriestreak.ui.AppViewModel
import com.sam.caloriestreak.ui.dashboard.DashboardScreen
import com.sam.caloriestreak.ui.grocery.GroceryScreen
import com.sam.caloriestreak.ui.history.HistoryScreen
import com.sam.caloriestreak.ui.ingredients.IngredientsScreen
import com.sam.caloriestreak.ui.meal_log.LogFoodScreen
import com.sam.caloriestreak.ui.recipes.RecipesScreen
import com.sam.caloriestreak.ui.statistics.StatisticsScreen

private data class Destination(val route: String, val label: String)

@Composable
fun CalorieStreakNavHost(appViewModel: AppViewModel = viewModel()) {
    val navController = rememberNavController()
    val state by appViewModel.state.collectAsStateWithLifecycle()
    val items = listOf(
        Destination("dashboard", "Dashboard"),
        Destination("log", "Log Food"),
        Destination("recipes", "Recipes"),
        Destination("grocery", "Grocery")
    )
    val entry by navController.currentBackStackEntryAsState()
    Scaffold(bottomBar = {
        NavigationBar {
            items.forEachIndexed { index, item ->
                NavigationBarItem(
                    selected = entry?.destination?.hierarchy?.any { it.route == item.route } == true,
                    onClick = { navController.navigate(item.route) { launchSingleTop = true } },
                    icon = {
                        val icon = when (index) {
                            0 -> Icons.Default.Dashboard
                            1 -> Icons.Default.PostAdd
                            2 -> Icons.Default.MenuBook
                            else -> Icons.Default.ShoppingCart
                        }
                        Icon(icon, contentDescription = item.label)
                    },
                    label = { Text(item.label) }
                )
            }
        }
    }) { padding ->
        NavHost(navController, startDestination = "dashboard", modifier = Modifier.padding(padding)) {
            composable("dashboard") {
                DashboardScreen(
                    state,
                    onLogFood = { navController.navigate("log") },
                    onIngredients = { navController.navigate("ingredients") },
                    onHistory = { navController.navigate("history") },
                    onStatistics = { navController.navigate("statistics") }
                )
            }
            composable("log") { LogFoodScreen(state.recipes, appViewModel::logRecipe, appViewModel::logManual) }
            composable("recipes") { RecipesScreen(state.ingredients, state.recipes, appViewModel::addRecipe) }
            composable("grocery") { GroceryScreen(state.recipes, state.groceryItems, appViewModel::generateGrocery, appViewModel::toggleGrocery, appViewModel::clearGrocery) }
            composable("ingredients") { IngredientsScreen(state.ingredients, appViewModel::addIngredient, appViewModel::deleteIngredient) }
            composable("history") { HistoryScreen(state.meals, appViewModel::deleteMeal) }
            composable("statistics") { StatisticsScreen(state.meals, state.currentStreak, state.bestStreak) }
        }
    }
}
