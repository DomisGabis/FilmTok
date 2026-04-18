package com.example.filmtok.model

data class Movie(
    val id: String = "",
    val title: String = "",
    val posterUrl: String = "",
    val backdropUrl: String = "",
    val videoUrl: String = "",
    val genres: List<String> = emptyList(),
    val year: Int = 2024,
    val description: String = "",
    val rating: Double = 0.0,
    val duration: String = "",
    val director: String = "",
    val cast: List<CastMember> = emptyList(),
    val gallery: List<String> = emptyList(),
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val isHero: Boolean = false,
    val hasVideo: Boolean = false
)

data class CastMember(
    val id: String = "",
    val name: String = "",
    val character: String = "",
    val imageUrl: String = ""
)
