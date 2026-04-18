package com.example.filmtok.viewmodel

import androidx.lifecycle.ViewModel
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
                Achievement("1", "Kolekcjoner", "Movie", "Dodaj 50 filmów do biblioteki", 0xFFFF2D55, true),
                Achievement("2", "Krytyk", "Star", "Wystaw 100 ocen", 0xFF00BFFF, true),
                Achievement("3", "Maratończyk", "Timer", "Oglądaj przez 10h bez przerwy", 0xFF8A2BE2, false)
            )
        )
    }
}
