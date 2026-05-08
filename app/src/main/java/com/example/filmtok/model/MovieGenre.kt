package com.example.filmtok.model

import androidx.annotation.StringRes
import com.example.filmtok.R

enum class MovieGenre(val id: Int, @StringRes val labelRes: Int) {
    ACTION(1, R.string.genre_action),
    ADVENTURE(2, R.string.genre_adventure),
    ANIMATION(3, R.string.genre_animation),
    BIOPIC(4, R.string.genre_biopic),
    COMEDY(5, R.string.genre_comedy),
    CRIME(6, R.string.genre_crime),
    DISASTER(7, R.string.genre_disaster),
    DOCUMENTARY(8, R.string.genre_documentary),
    DRAMA(9, R.string.genre_drama),
    FANTASY(10, R.string.genre_fantasy),
    HORROR(11, R.string.genre_horror),
    MUSICAL(12, R.string.genre_musical),
    MYSTERY(13, R.string.genre_mystery),
    ROMANCE(14, R.string.genre_romance),
    ROMCOM(15, R.string.genre_romcom),
    SCI_FI(16, R.string.genre_sci_fi),
    SUPERHERO(17, R.string.genre_superhero),
    THRILLER(18, R.string.genre_thriller),
    WAR(19, R.string.genre_war),
    WESTERN(20, R.string.genre_western);

    companion object {
        fun fromId(id: Int): MovieGenre? = MovieGenre.entries.find { it.id == id }

        fun fromString(name: String): MovieGenre? = MovieGenre.entries.find { it.name.equals(name, ignoreCase = true) }
    }
}