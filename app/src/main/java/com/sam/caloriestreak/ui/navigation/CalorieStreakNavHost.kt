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
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sam.caloriestreak.ui.screens.PlaceholderScreen

private data class Destination(val route: String, val label: String)

@Composable
fun CalorieStreakNavHost() {
    val navController = rememberNavController()
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
            composable("dashboard") { PlaceholderScreen("Dashboard", "Today: 0 kcal · Score: 0%") }
            composable("log") { PlaceholderScreen("Log Food", "Fast meal logging will be implemented here.") }
            composable("recipes") { PlaceholderScreen("Recipes", "Recipe list placeholder") }
            composable("grocery") { PlaceholderScreen("Grocery List", "Grocery list placeholder") }
        }
    }
}
