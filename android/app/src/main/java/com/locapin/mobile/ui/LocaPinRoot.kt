package com.locapin.mobile.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.locapin.mobile.core.navigation.Routes
import com.locapin.mobile.feature.auth.ForgotPasswordScreen
import com.locapin.mobile.feature.auth.LoginScreen
import com.locapin.mobile.feature.auth.RegisterScreen
import com.locapin.mobile.feature.destination.DestinationDetailsScreen
import com.locapin.mobile.feature.explore.ExploreScreen
import com.locapin.mobile.feature.favorites.FavoritesScreen
import com.locapin.mobile.feature.home.HomeScreen
import com.locapin.mobile.feature.home.OnboardingScreen
import com.locapin.mobile.feature.home.SplashScreen
import com.locapin.mobile.feature.map.MapScreen
import com.locapin.mobile.feature.profile.ProfileScreen
import com.locapin.mobile.feature.search.SearchScreen
import com.locapin.mobile.feature.settings.SettingsScreen

private data class BottomItem(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

@Composable
fun LocaPinRoot(hasLocationPermission: Boolean, requestLocationPermission: () -> Unit, vm: MainViewModel = hiltViewModel()) {
    val navController = rememberNavController()
    val state by vm.state.collectAsStateWithLifecycle()
    val currentDestination = navController.currentBackStackEntryAsState().value?.destination

    LaunchedEffect(state.loading, state.onboarded, state.authed) {
        if (!state.loading) {
            val route = when {
                !state.onboarded -> Routes.Onboarding
                !state.authed -> Routes.Login
                else -> Routes.Home
            }
            navController.navigate(route) {
                popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
            }
        }
    }

    val showBottom = currentDestination?.route in setOf(Routes.Home, Routes.Explore, Routes.Map, Routes.Favorites, Routes.Profile)
    val bottomItems = listOf(
        BottomItem(Routes.Home, "Home", Icons.Default.Home),
        BottomItem(Routes.Explore, "Explore", Icons.Default.Explore),
        BottomItem(Routes.Map, "Map", Icons.Default.Map),
        BottomItem(Routes.Favorites, "Favorites", Icons.Default.Favorite),
        BottomItem(Routes.Profile, "Profile", Icons.Default.Person)
    )

    Scaffold(
        modifier = Modifier,
        bottomBar = {
            if (showBottom) {
                NavigationBar {
                    bottomItems.forEach { item ->
                        NavigationBarItem(
                            selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                            onClick = {
                                navController.navigate(item.route) {
                                    launchSingleTop = true
                                    restoreState = true
                                    popUpTo(Routes.Home) { saveState = true }
                                }
                            },
                            icon = { Icon(item.icon, item.label) },
                            label = { Text(item.label) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(navController = navController, startDestination = Routes.Splash) {
            composable(Routes.Splash) { SplashScreen() }
            composable(Routes.Onboarding) {
                OnboardingScreen(
                    onDone = { vm.completeOnboarding() },
                    onSkip = { vm.completeOnboarding() }
                )
            }
            composable(Routes.Login) {
                LoginScreen(
                    onRegister = { navController.navigate(Routes.Register) },
                    onForgotPassword = { navController.navigate(Routes.ForgotPassword) },
                    onSuccess = { navController.navigate(Routes.Home) }
                )
            }
            composable(Routes.Register) {
                RegisterScreen(onBack = { navController.popBackStack() }, onSuccess = { navController.navigate(Routes.Home) })
            }
            composable(Routes.ForgotPassword) { ForgotPasswordScreen(onBack = { navController.popBackStack() }) }

            composable(Routes.Home) { HomeScreen(vm, onOpenMap = { navController.navigate(Routes.Map) }, onDetails = { navController.navigate("${Routes.DestinationDetailsBase}/$it") }) }
            composable(Routes.Explore) { ExploreScreen(vm, onDetails = { navController.navigate("${Routes.DestinationDetailsBase}/$it") }) }
            composable(Routes.Map) {
                MapScreen(
                    vm = vm,
                    hasLocationPermission = hasLocationPermission,
                    requestPermission = requestLocationPermission,
                    onDetails = { navController.navigate("${Routes.DestinationDetailsBase}/$it") }
                )
            }
            composable(Routes.Favorites) { FavoritesScreen(vm, onDetails = { navController.navigate("${Routes.DestinationDetailsBase}/$it") }) }
            composable(Routes.Profile) { ProfileScreen(vm, onSettings = { navController.navigate(Routes.Settings) }) }
            composable(Routes.Search) { SearchScreen(vm, onDetails = { navController.navigate("${Routes.DestinationDetailsBase}/$it") }) }
            composable(Routes.Settings) { SettingsScreen() }
            composable(Routes.DestinationDetails) {
                val id = it.arguments?.getString("id").orEmpty()
                DestinationDetailsScreen(vm, destinationId = id, onBack = { navController.popBackStack() })
            }
        }
    }
}
