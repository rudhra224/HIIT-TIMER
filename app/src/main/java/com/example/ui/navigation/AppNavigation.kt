package com.example.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.TimerProfile
import com.example.ui.screens.ActiveTimerScreen
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.RoutinesScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.viewmodel.ActiveTimerViewModel
import com.example.ui.viewmodel.AnalyticsViewModel
import com.example.ui.viewmodel.RoutinesViewModel
import com.example.ui.viewmodel.SettingsViewModel

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Routines : Screen("routines", "Routines", Icons.Default.FormatListBulleted)
    object ActiveTimer : Screen("active_timer", "Active Timer", Icons.Default.Timer)
    object Analytics : Screen("analytics", "Analytics", Icons.Default.BarChart)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
}

@Composable
fun AppNavigation(
    routinesViewModel: RoutinesViewModel = viewModel(),
    activeTimerViewModel: ActiveTimerViewModel = viewModel(),
    analyticsViewModel: AnalyticsViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    var selectedWorkoutProfile by remember { mutableStateOf<TimerProfile?>(null) }

    val items = listOf(
        Screen.Routines,
        Screen.ActiveTimer,
        Screen.Analytics,
        Screen.Settings
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                modifier = Modifier.testTag("bottom_navigation_bar")
            ) {
                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = currentRoute == screen.route,
                        onClick = {
                            if (currentRoute != screen.route) {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        modifier = Modifier.testTag("nav_item_${screen.route}")
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Routines.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable(Screen.Routines.route) {
                RoutinesScreen(
                    viewModel = routinesViewModel,
                    onStartWorkout = { profile ->
                        selectedWorkoutProfile = profile
                        activeTimerViewModel.startTimerWithProfile(profile)
                        navController.navigate(Screen.ActiveTimer.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(Screen.ActiveTimer.route) {
                ActiveTimerScreen(
                    viewModel = activeTimerViewModel,
                    selectedProfile = selectedWorkoutProfile,
                    onNavigateBackToRoutines = {
                        navController.navigate(Screen.Routines.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(Screen.Analytics.route) {
                AnalyticsScreen(
                    viewModel = analyticsViewModel
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    viewModel = settingsViewModel
                )
            }
        }
    }
}
