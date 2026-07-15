package com.sam.caloriestreak.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sam.caloriestreak.ui.AppViewModel
import com.sam.caloriestreak.ui.FeatureViewModel
import com.sam.caloriestreak.ui.achievements.AchievementPopupHost
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
private val dashboardChildren = setOf("history", "statistics")
private val moreChildren = setOf("grocery", "weight", "achievements", "settings")

@Composable
fun CalorieStreakNavHost(
    appViewModel: AppViewModel = viewModel(),
    featureViewModel: FeatureViewModel = viewModel()
) {
    val navController = rememberNavController()
    val state by appViewModel.state.collectAsStateWithLifecycle()
    val featureState by featureViewModel.state.collectAsStateWithLifecycle()
    val items = listOf(
        Destination("dashboard", "Dashboard"),
        Destination("log", "Log Food"),
        Destination("recipes", "Recipes"),
        Destination("more", "More")
    )
    val entry by navController.currentBackStackEntryAsState()
    val currentRoute = entry?.destination?.route

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(
                modifier = Modifier.navigationBarsPadding(),
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                tonalElevation = 6.dp
            ) {
                items.forEachIndexed { index, item ->
                    val selected = when (item.route) {
                        "dashboard" -> currentRoute == "dashboard" || currentRoute in dashboardChildren
                        "more" -> currentRoute == "more" || currentRoute in moreChildren
                        else -> entry?.destination?.hierarchy?.any { it.route == item.route } == true
                    }
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(item.route) {
                                launchSingleTop = true
                                restoreState = true
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        icon = {
                            val icon = when (index) {
                                0 -> Icons.Default.Dashboard
                                1 -> Icons.Default.PostAdd
                                2 -> Icons.Default.MenuBook
                                else -> Icons.Default.Menu
                            }
                            Icon(icon, contentDescription = item.label)
                        },
                        label = { Text(item.label) }
                    )
                }
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize()) {
            NavHost(navController, startDestination = "dashboard", modifier = Modifier.padding(padding)) {
                composable("dashboard") {
                    DashboardScreen(
                        state = state,
                        onHistory = { navController.navigate("history") },
                        onStatistics = { navController.navigate("statistics") },
                        onFreezeToday = {
                            if (state.freezes == 1) featureViewModel.recordLastFreezeUsed()
                            appViewModel.freezeToday()
                        },
                        onDeleteMeal = appViewModel::deleteMeal
                    )
                }
                composable("log") { LogFoodScreen(state.recipes, appViewModel::logRecipe, appViewModel::logManual) }
                composable("recipes") {
                    RecipesScreen(
                        ingredients = state.allIngredients,
                        recipes = state.allRecipes,
                        onSave = appViewModel::saveRecipe,
                        onOpenIngredients = { navController.navigate("ingredients") { launchSingleTop = true } }
                    )
                }
                composable("history") {
                    HistoryScreen(
                        meals = state.meals,
                        dailyLogs = state.dailyLogs,
                        weights = featureState.weights,
                        targetCalories = state.target,
                        weightGoal = featureState.weightGoal,
                        onDelete = appViewModel::deleteMeal
                    )
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
                    GroceryScreen(
                        recipes = state.recipes,
                        ingredients = state.ingredients,
                        items = state.groceryItems,
                        onGenerate = { recipe, multiplier ->
                            appViewModel.generateGrocery(recipe, multiplier)
                            featureViewModel.recordGroceryGenerated()
                        },
                        onAddIngredient = appViewModel::addIngredientToGrocery,
                        onToggle = { item ->
                            featureViewModel.recordGroceryToggle(state.groceryItems, item)
                            appViewModel.toggleGrocery(item)
                        },
                        onClear = appViewModel::clearGrocery
                    )
                }
                composable("weight") {
                    WeightScreen(
                        entries = featureState.weights,
                        stats = featureState.weightStats,
                        weightGoal = featureState.weightGoal,
                        onAdd = featureViewModel::addWeight,
                        onUpdate = featureViewModel::updateWeight,
                        onDelete = featureViewModel::deleteWeight
                    )
                }
                composable("achievements") { AchievementsScreen(featureState.earned, featureViewModel::markAchievementsSeen) }
                composable("settings") {
                    SettingsScreen(
                        calorieTarget = state.target,
                        weightGoal = featureState.weightGoal,
                        freezeRequiredDays = state.freezeRequiredDays,
                        onSave = { calorieTarget, weightGoal ->
                            featureViewModel.setGoals(calorieTarget, weightGoal).fold(
                                onSuccess = { appViewModel.setDailyTarget(calorieTarget) },
                                onFailure = { Result.failure(it) }
                            )
                        }
                    )
                }
                composable("ingredients") {
                    IngredientsScreen(
                        ingredients = state.allIngredients,
                        onSave = appViewModel::saveIngredient,
                        onDelete = appViewModel::deleteIngredient,
                        onOpenRecipes = {
                            navController.navigate("recipes") {
                                launchSingleTop = true
                                popUpTo("recipes") { inclusive = false }
                            }
                        }
                    )
                }
                composable("statistics") {
                    StatisticsScreen(
                        meals = state.meals,
                        currentStreak = state.currentStreak,
                        bestStreak = state.bestStreak,
                        targetCalories = state.target,
                        freezes = state.freezes,
                        freezeProgress = state.freezeProgress,
                        freezeRequiredDays = state.freezeRequiredDays,
                        weight = featureState.weightStats,
                        earnedAchievements = featureState.earned.size,
                        totalAchievements = featureState.totalAchievements
                    )
                }
            }

            AchievementPopupHost(
                pendingAchievements = featureState.pendingAchievementPopups,
                pendingSummary = featureState.pendingPopupSummary,
                onDismissAchievement = featureViewModel::dismissAchievementPopup,
                onDismissSummary = featureViewModel::dismissPopupSummary,
                onOpenAchievements = {
                    navController.navigate("achievements") { launchSingleTop = true }
                }
            )
        }
    }
}
