package com.sam.caloriestreak.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PostAdd
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
import com.sam.caloriestreak.domain.calculation.ScoreCalculator
import com.sam.caloriestreak.ui.AppViewModel
import com.sam.caloriestreak.ui.FeatureViewModel
import com.sam.caloriestreak.ui.achievements.AchievementsScreen
import com.sam.caloriestreak.ui.dashboard.DashboardScreen
import com.sam.caloriestreak.ui.grocery.GroceryScreen
import com.sam.caloriestreak.ui.history.HistoryScreen
import com.sam.caloriestreak.ui.ingredients.IngredientsScreen
import com.sam.caloriestreak.ui.meal_log.LogFoodScreen
import com.sam.caloriestreak.ui.more.MoreScreen
import com.sam.caloriestreak.ui.recipes.RecipesScreen
import com.sam.caloriestreak.ui.settings.SettingsScreen
import com.sam.caloriestreak.ui.statistics.StatisticsScreen
import com.sam.caloriestreak.ui.weight.WeightScreen

private data class Destination(val route: String, val label: String)
private val moreChildren = setOf("grocery", "weight", "achievements", "settings")

@Composable
fun CalorieStreakNavHost(
    appViewModel: AppViewModel = viewModel(),
    featureViewModel: FeatureViewModel = viewModel()
) {
    val navController = rememberNavController()
    val appState by appViewModel.state.collectAsStateWithLifecycle()
    val featureState by featureViewModel.state.collectAsStateWithLifecycle()
    val scoreCalculator = ScoreCalculator.forTarget(featureState.target)
    val state = appState.copy(
        target = featureState.target,
        todayScore = scoreCalculator.calculate(appState.todayCalories),
        todayEffectiveScore = if (appState.todayFrozen) 100.0 else scoreCalculator.calculate(appState.todayCalories),
        status = if (appState.todayFrozen) "Freeze active" else scoreCalculator.status(scoreCalculator.calculate(appState.todayCalories)),
        weights = featureState.weights,
        weightStats = featureState.weightStats,
        earnedAchievements = featureState.earned,
        achievementTotal = featureState.totalAchievements,
        unseenAchievementCount = featureState.unseenCount
    )
    val items = listOf(
        Destination("dashboard", "Dashboard"),
        Destination("log", "Log Food"),
        Destination("recipes", "Recipes"),
        Destination("history", "History"),
        Destination("more", "More")
    )
    val entry by navController.currentBackStackEntryAsState()
    val currentRoute = entry?.destination?.route

    Scaffold(bottomBar = {
        NavigationBar {
            items.forEachIndexed { index, item ->
                val selected = if (item.route == "more") currentRoute == "more" || currentRoute in moreChildren else entry?.destination?.hierarchy?.any { it.route == item.route } == true
                NavigationBarItem(
                    selected = selected,
                    onClick = {
                        navController.navigate(item.route) {
                            launchSingleTop = true
                            restoreState = true
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                        }
                    },
                    icon = {
                        val icon = when (index) {
                            0 -> Icons.Default.Dashboard
                            1 -> Icons.Default.PostAdd
                            2 -> Icons.Default.MenuBook
                            3 -> Icons.Default.History
                            else -> Icons.Default.Menu
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
                    state = state,
                    onHistory = { navController.navigate("history") },
                    onStatistics = { navController.navigate("statistics") },
                    onFreezeToday = appViewModel::freezeToday,
                    onDeleteMeal = appViewModel::deleteMeal
                )
            }
            composable("log") { LogFoodScreen(state.recipes, appViewModel::logRecipe, appViewModel::logManual) }
            composable("recipes") {
                RecipesScreen(
                    ingredients = state.ingredients,
                    recipes = state.recipes,
                    onAdd = appViewModel::addRecipe,
                    onOpenIngredients = { navController.navigate("ingredients") { launchSingleTop = true } }
                )
            }
            composable("history") {
                HistoryScreen(meals = state.meals, dailyLogs = state.dailyLogs, targetCalories = state.target, onDelete = appViewModel::deleteMeal)
            }
            composable("more") {
                MoreScreen(
                    latestWeight = featureState.weightStats.latest,
                    earnedCount = featureState.earned.size,
                    totalCount = featureState.totalAchievements,
                    unseenCount = featureState.unseenCount,
                    onGrocery = { navController.navigate("grocery") },
                    onWeight = { navController.navigate("weight") },
                    onAchievements = { navController.navigate("achievements") },
                    onSettings = { navController.navigate("settings") }
                )
            }
            composable("grocery") {
                GroceryScreen(state.recipes, state.ingredients, state.groceryItems, appViewModel::generateGrocery, appViewModel::addIngredientToGrocery, appViewModel::toggleGrocery, appViewModel::clearGrocery)
            }
            composable("weight") {
                WeightScreen(featureState.weights, featureState.weightStats, featureViewModel::addWeight, featureViewModel::updateWeight, featureViewModel::deleteWeight)
            }
            composable("achievements") { AchievementsScreen(featureState.earned, featureViewModel::markAchievementsSeen) }
            composable("settings") { SettingsScreen(featureState.target, featureViewModel::setTarget) }
            composable("ingredients") {
                IngredientsScreen(state.ingredients, appViewModel::addIngredient, appViewModel::deleteIngredient) {
                    navController.navigate("recipes") { launchSingleTop = true; popUpTo("recipes") { inclusive = false } }
                }
            }
            composable("statistics") { StatisticsScreen(state.meals, state.currentStreak, state.bestStreak) }
        }
    }
}
