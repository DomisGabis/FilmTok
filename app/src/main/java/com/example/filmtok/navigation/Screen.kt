package com.example.filmtok.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String = "", val icon: ImageVector? = null) {
    object Home : Screen("home", "Główna", Icons.Default.Home)
    object Reels : Screen("reels", "Rolki", Icons.Default.PlayArrow)
    object Search : Screen("search", "Szukaj", Icons.Default.Search)
    object Profile : Screen("profile", "Profil", Icons.Default.Person)
    object Login : Screen("login")
    object Register : Screen("register")
    object AdminDashboard : Screen("admin_dashboard")
    object AdminAddMovie : Screen("admin_add_movie")

    object MovieDetails : Screen("movie_details/{movieId}") {
        fun createRoute(movieId: String) = "movie_details/$movieId"
    }
}
