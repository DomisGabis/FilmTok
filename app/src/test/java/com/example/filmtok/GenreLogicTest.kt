package com.example.filmtok

import com.example.filmtok.data.UserRepository
import com.example.filmtok.model.Movie
import com.example.filmtok.model.MovieGenre
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GenreLogicTest {
    @Test
    fun `getTop3FavoriteGenres should return 3 most frequent genres in correct order`() {
        val movies = listOf(
            Movie(id = "1", genres = listOf(MovieGenre.ACTION, MovieGenre.DRAMA)),
            Movie(id = "2", genres = listOf(MovieGenre.ACTION, MovieGenre.SCI_FI)),
            Movie(id = "3", genres = listOf(MovieGenre.ACTION, MovieGenre.SCI_FI)),
            Movie(id = "4", genres = listOf(MovieGenre.DRAMA, MovieGenre.COMEDY))
        )

        val result = UserRepository.getTop3FavoriteGenres(movies)

        assertEquals(3, result.size)
        assertEquals(MovieGenre.ACTION, result[0])

        assertTrue(result.contains(MovieGenre.SCI_FI))
        assertTrue(result.contains(MovieGenre.DRAMA))
    }

    @Test
    fun `getTop3FavoriteGenres should return empty list when input is empty`() {
        val movies = emptyList<Movie>()
        val result = UserRepository.getTop3FavoriteGenres(movies)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `getTop3FavoriteGenres should return all unique genres when there are fewer than 3`() {
        val movies = listOf(
            Movie(id = "1", genres = listOf(MovieGenre.ACTION)),
            Movie(id = "2", genres = listOf(MovieGenre.ACTION, MovieGenre.COMEDY))
        )
        val result = UserRepository.getTop3FavoriteGenres(movies)
        assertEquals(2, result.size)
        assertEquals(MovieGenre.ACTION, result[0])
        assertEquals(MovieGenre.COMEDY, result[1])
    }

    @Test
    fun `getTop3FavoriteGenres should return exactly 3 genres when there are more available`() {
        val movies = listOf(
            Movie(id = "1", genres = listOf(MovieGenre.ACTION, MovieGenre.COMEDY, MovieGenre.DRAMA, MovieGenre.HORROR))
        )
        val result = UserRepository.getTop3FavoriteGenres(movies)
        assertEquals(3, result.size)
    }

    @Test
    fun `getTop3FavoriteGenres should ignore movies with no genres`() {
        val movies = listOf(
            Movie(id = "1", genres = emptyList()),
            Movie(id = "2", genres = listOf(MovieGenre.ROMANCE))
        )
        val result = UserRepository.getTop3FavoriteGenres(movies)
        assertEquals(1, result.size)
        assertEquals(MovieGenre.ROMANCE, result[0])
    }

    @Test
    fun `getTop3FavoriteGenres should correctly handle ties in frequency`() {
        val movies = listOf(
            Movie(id = "1", genres = listOf(MovieGenre.ACTION)),
            Movie(id = "2", genres = listOf(MovieGenre.COMEDY)),
            Movie(id = "3", genres = listOf(MovieGenre.DRAMA))
        )
        val result = UserRepository.getTop3FavoriteGenres(movies)
        assertEquals(3, result.size)
        assertTrue(result.contains(MovieGenre.ACTION))
        assertTrue(result.contains(MovieGenre.COMEDY))
        assertTrue(result.contains(MovieGenre.DRAMA))
    }

    @Test
    fun `getTop3FavoriteGenres should not duplicate genres from same movie`() {
        val movies = listOf(
            Movie(id = "1", genres = listOf(MovieGenre.ACTION, MovieGenre.ACTION))
        )
        val result = UserRepository.getTop3FavoriteGenres(movies)
        assertEquals(1, result.size)
        assertEquals(MovieGenre.ACTION, result[0])
    }
}
