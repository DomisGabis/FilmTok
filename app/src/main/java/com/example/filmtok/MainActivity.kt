package com.example.filmtok

import androidx.compose.runtime.saveable.rememberSaveable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.filmtok.data.MovieRepository
import com.example.filmtok.model.Movie
import com.example.filmtok.navigation.Screen
import com.example.filmtok.ui.screens.*
import com.example.filmtok.ui.theme.FilmTokTheme
import com.example.filmtok.viewmodel.AuthViewModel
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        enableEdgeToEdge()
        setContent {
            var isDarkMode by rememberSaveable { mutableStateOf(true) }

            FilmTokTheme(darkTheme = isDarkMode) {
                val authViewModel: AuthViewModel = viewModel()
                val userRole by authViewModel.userRole.collectAsState()
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                val bottomBarItems = remember(userRole) {
                    if (userRole == "admin") {
                        listOf(
                            Screen.Home,
                            Screen.Reels,
                            Screen.Search,
                            Screen.AdminDashboard,
                            Screen.Profile
                        )
                    } else {
                        listOf(
                            Screen.Home,
                            Screen.Reels,
                            Screen.Search,
                            Screen.Profile
                        )
                    }
                }

                val showBottomBar = bottomBarItems.any { it.route == currentDestination?.route }

                Scaffold(
                    bottomBar = {
                        if (showBottomBar) {
                            NavigationBar(
                                containerColor = MaterialTheme.colorScheme.surface,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            ) {
                                bottomBarItems.forEach { screen ->
                                    val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                                    val label = screen.titleRes?.let { stringResource(it) } ?: ""
                                    NavigationBarItem(
                                        icon = { screen.icon?.let { Icon(it, contentDescription = label) } },
                                        label = { Text(label) },
                                        selected = selected,
                                        onClick = {
                                            navController.navigate(screen.route) {
                                                popUpTo(navController.graph.findStartDestination().id) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = MaterialTheme.colorScheme.primary,
                                            selectedTextColor = MaterialTheme.colorScheme.primary,
                                            unselectedIconColor = Color.Gray,
                                            unselectedTextColor = Color.Gray,
                                            indicatorColor = Color.Transparent
                                        )
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = if (FirebaseAuth.getInstance().currentUser != null) Screen.Home.route else Screen.Login.route,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(Screen.Login.route) {
                            LoginScreen(
                                onLoginSuccess = { uid -> authViewModel.checkUserRole(uid) },
                                onNavigateToRegister = { navController.navigate(Screen.Register.route) }
                            )
                        }
                        
                        composable(Screen.Register.route) {
                            RegisterScreen(
                                onRegisterSuccess = {
                                    navController.navigate(Screen.Home.route) {
                                        popUpTo(Screen.Login.route) { inclusive = true }
                                    }
                                },
                                onNavigateToLogin = { navController.popBackStack() }
                            )
                        }

                        composable(Screen.Home.route) {
                            HomeScreen(onMovieClick = { movieId ->
                                navController.navigate(Screen.MovieDetails.createRoute(movieId))
                            })
                        }
                        
                        composable(Screen.Reels.route) {
                            ReelsScreen(onSeeDetailsClick = { movieId ->
                                navController.navigate(Screen.MovieDetails.createRoute(movieId))
                            })
                        }
                        
                        composable(Screen.Search.route) {
                            SearchScreen(onMovieClick = { movieId ->
                                navController.navigate(Screen.MovieDetails.createRoute(movieId))
                            })
                        }
                        
                        composable(Screen.Profile.route) {
                            UserProfileScreen(
                                isDarkMode = isDarkMode,
                                onDarkModeChange = { isDarkMode = it },
                                onLogout = {
                                    authViewModel.signOut()
                                    navController.navigate(Screen.Login.route) {
                                        popUpTo(0)
                                    }
                                }
                            )
                        }

                        composable(Screen.AdminDashboard.route) {
                            AdminDashboardScreen(
                                onLogout = {
                                    authViewModel.signOut()
                                    navController.navigate(Screen.Login.route) { popUpTo(0) }
                                },
                                onNavigateToAddMovie = {
                                    navController.navigate(Screen.AdminAddMovie.createRoute())
                                },
                                onNavigateToEditMovie = { movieId ->
                                    navController.navigate(Screen.AdminAddMovie.createRoute(movieId))
                                }
                            )
                        }

                        composable(
                            route = Screen.AdminAddMovie.route,
                            arguments = listOf(navArgument("movieId") { 
                                nullable = true
                                defaultValue = null
                                type = NavType.StringType 
                            })
                        ) { backStackEntry ->
                            val movieId = backStackEntry.arguments?.getString("movieId")
                            AdminAddMovieScreen(
                                movieId = movieId,
                                onBackClick = { navController.popBackStack() }
                            )
                        }

                        composable(
                            route = Screen.MovieDetails.route,
                            arguments = listOf(navArgument("movieId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val movieId = backStackEntry.arguments?.getString("movieId") ?: ""
                            MovieDetailsScreen(
                                movieId = movieId,
                                onBackClick = { navController.popBackStack() }
                            )
                        }
                    }

                    LaunchedEffect(userRole) {
                        val currentRoute = navController.currentDestination?.route
                        if (currentRoute == Screen.Login.route || currentRoute == null) {
                            when (userRole) {
                                "admin" -> navController.navigate(Screen.AdminDashboard.route) {
                                    popUpTo(Screen.Login.route) { inclusive = true }
                                }
                                "user" -> navController.navigate(Screen.Home.route) {
                                    popUpTo(Screen.Login.route) { inclusive = true }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PlaceholderScreen(name: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "Ekran: $name", color = Color.White)
    }
}


