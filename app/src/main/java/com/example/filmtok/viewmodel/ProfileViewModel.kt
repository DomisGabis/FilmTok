package com.example.filmtok.viewmodel

import androidx.lifecycle.ViewModel
import com.example.filmtok.R
import com.example.filmtok.model.Achievement
import com.example.filmtok.model.User
import com.example.filmtok.model.UserStats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ProfileViewModel : ViewModel() {
    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    init {
        loadUserData()
    }

    private fun loadUserData() {
        // Mock data for now
        _user.value = User(
            id = "1",
            name = "Alex Cinemaphile",
            username = "alex_c",
            profileImageUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?q=80&w=1974&auto=format&fit=crop",
            stats = UserStats(
                moviesInBase = 2,
                averageRating = 4.7,
                watchTimeHours = 5,
                moviesWatched = 142
            ),
            favoriteGenres = listOf("Sci-Fi", "Action", "Drama"),
            achievements = listOf(
                Achievement("1", R.string.achievement_collector_title, "Movie", R.string.achievement_collector_desc, 0xFFFF2D55, true),
                Achievement("2", R.string.achievement_critic_title, "Star", R.string.achievement_critic_desc, 0xFF00BFFF, true),
                Achievement("3", R.string.achievement_marathoner_title, "Timer", R.string.achievement_marathoner_desc, 0xFF8A2BE2, false)
            )
        )
    }
}
