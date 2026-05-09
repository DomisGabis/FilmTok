package com.example.filmtok

import com.example.filmtok.data.UserRepository
import com.example.filmtok.model.Movie
import com.example.filmtok.model.MovieGenre
import com.example.filmtok.viewmodel.SearchViewModel
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertFalse
import junit.framework.Assert.assertTrue
import org.junit.Test

class SearchLogicTest {

    @Test
    fun `filterMovies should return all movies when query is empty and genre is all`() {
        val movies = listOf(
            Movie(title = "F1"),
            Movie(title = "F2")
        )
        val result = SearchViewModel.filterMovies(movies, "", R.string.genre_all)
        assertEquals(2, result.size)
    }

    @Test
    fun `filterMovies should filter by title case-insensitively`() {
        val movies = listOf(
            Movie(title = "The Batman"),
            Movie(title = "Dune"),
            Movie(title = "Batman Begins")
        )
        val result = SearchViewModel.filterMovies(movies, "baTmAn", R.string.genre_all)
        assertEquals(2, result.size)
        assertTrue(result.any { it.title == "The Batman" })
        assertTrue(result.any { it.title == "Batman Begins" })
    }

    @Test
    fun `filterMovies should filter by director`() {
        val movies = listOf(
            Movie(title = "Dune", director = "Denis Villeneuve"),
            Movie(title = "Interstellar", director = "Christopher Nolan")
        )
        val result = SearchViewModel.filterMovies(movies, "Nolan", R.string.genre_all)
        assertEquals(1, result.size)
        assertEquals("Interstellar", result[0].title)
    }

    @Test
    fun `filterMovies should filter by genre label resource`() {
        val movies = listOf(
            Movie(title = "Movie 1", genres = listOf(MovieGenre.ACTION)),
            Movie(title = "Movie 2", genres = listOf(MovieGenre.COMEDY))
        )
        val result = SearchViewModel.filterMovies(movies, "", MovieGenre.ACTION.labelRes)
        assertEquals(1, result.size)
        assertEquals("Movie 1", result[0].title)
    }

    @Test
    fun `filterMovies should combine query and genre filtering`() {
        val movies = listOf(
            Movie(title = "Batman Begins", genres = listOf(MovieGenre.DRAMA)),
            Movie(title = "Spider-Man", genres = listOf(MovieGenre.ACTION)),
            Movie(title = "Avengers", genres = listOf(MovieGenre.ACTION))
        )
        val result = SearchViewModel.filterMovies(movies, "Man", MovieGenre.ACTION.labelRes)
        assertEquals(1, result.size)
        assertEquals("Spider-Man", result[0].title)
    }

    @Test
    fun `filterMovies should return empty list when no matches found`() {
        val movies = listOf(
            Movie(title = "Dune")
        )
        val result = SearchViewModel.filterMovies(movies, "Star Wars", R.string.genre_all)
        assertTrue(result.isEmpty())
    }
}
