package com.example.filmtok.model

import androidx.annotation.StringRes

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
    @StringRes val titleRes: Int = 0,
    val iconName: String = "",
    @StringRes val descriptionRes: Int = 0,
    val color: Long = 0xFFFF2D55,
    val isUnlocked: Boolean = false
)
