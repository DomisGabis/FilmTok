package com.example.filmtok.model

data class User(
    val id: String = "",
    val name: String = "",
    val username: String = "",
    val profileImageUrl: String = "",
    val stats: UserStats = UserStats(),
    val favoriteGenres: List<String> = emptyList(),
    val achievements: List<Achievement> = emptyList(),
    val isAdmin: Boolean = false
)

data class UserStats(
    val moviesInBase: Int = 0,
    val averageRating: Double = 0.0,
    val watchTimeHours: Int = 0,
    val moviesWatched: Int = 0
)

data class Achievement(
    val id: String = "",
    val title: String = "",
    val iconName: String = "",
    val description: String = "",
    val color: Long = 0xFFFF2D55,
    val isUnlocked: Boolean = false
)
