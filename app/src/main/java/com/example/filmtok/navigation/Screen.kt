package com.example.filmtok.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.filmtok.R

sealed class Screen(val route: String, @StringRes val titleRes: Int? = null, val icon: ImageVector? = null) {
    object Home : Screen("home", R.string.nav_home, Icons.Default.Home)
    object Reels : Screen("reels", R.string.nav_reels, Icons.Default.PlayArrow)
    object Search : Screen("search", R.string.nav_search, Icons.Default.Search)
    object Profile : Screen("profile", R.string.nav_profile, Icons.Default.Person)
    object Login : Screen("login")
    object Register : Screen("register")
    object AdminDashboard : Screen("admin_dashboard", R.string.nav_admin, Icons.Default.Settings)
    object AdminAddMovie : Screen("admin_add_movie?movieId={movieId}") {
        fun createRoute(movieId: String? = null) = if (movieId != null) "admin_add_movie?movieId=$movieId" else "admin_add_movie"
    }

    object MovieDetails : Screen("movie_details/{movieId}") {
        fun createRoute(movieId: String) = "movie_details/$movieId"
    }
}
