package com.example.filmtok.model

import androidx.annotation.StringRes

data class User(
    val id: String = "",
    val email: String = "",
    val profileImageUrl: String = "",
    val favoriteMovies: List<String> = emptyList(),
    val favoriteGenres: List<MovieGenre> = emptyList(),
    val isAdmin: Boolean = false
)