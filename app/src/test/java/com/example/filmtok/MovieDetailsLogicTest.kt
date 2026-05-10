package com.example.filmtok

import com.example.filmtok.data.MovieRepository
import com.example.filmtok.data.StorageRepository
import com.example.filmtok.data.UserRepository
import com.example.filmtok.model.Movie
import com.example.filmtok.viewmodel.MovieDetailsViewModel
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MovieDetailsLogicTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: MovieDetailsViewModel
    private val movieRepository = mockk<MovieRepository>(relaxed = true)
    private val storageRepository = mockk<StorageRepository>(relaxed = true)
    private val userRepository = mockk<UserRepository>(relaxed = true)

    @Before
    fun setup() {
        coEvery { storageRepository.resolveMovieUrls(any()) } answers { firstArg() }
        
        viewModel = MovieDetailsViewModel(movieRepository, storageRepository, userRepository)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `loadMovieDetails should set isFavorite to true if movie is in favorites`() = runTest {
        val movieId = "movie123"
        val movie = Movie(id = movieId, title = "Test Movie")
        coEvery { movieRepository.getMovieDetails(movieId) } returns movie
        coEvery { userRepository.isMovieFavorite(movieId) } returns true

        viewModel.loadMovieDetails(movieId)

        assertTrue(viewModel.isFavorite.value)
        assertEquals(movie, viewModel.movie.value)
    }

    @Test
    fun `toggleFavorite should add movie and update state when not favorite`() = runTest {
        val movie = Movie(id = "123", title = "Inception")
        coEvery { movieRepository.getMovieDetails(any()) } returns movie
        coEvery { userRepository.isMovieFavorite(any()) } returns false
        
        viewModel.loadMovieDetails("123")
        assertFalse(viewModel.isFavorite.value)

        viewModel.toggleFavorite()

        coVerify { userRepository.addFavoriteMovie("123") }
        assertTrue(viewModel.isFavorite.value)
    }

    @Test
    fun `toggleFavorite should remove movie and update state when already favorite`() = runTest {
        val movie = Movie(id = "123", title = "Inception")
        coEvery { movieRepository.getMovieDetails(any()) } returns movie
        coEvery { userRepository.isMovieFavorite(any()) } returns true
        
        viewModel.loadMovieDetails("123")
        assertTrue(viewModel.isFavorite.value)

        viewModel.toggleFavorite()

        coVerify { userRepository.removeFavoriteMovie("123") }
        assertFalse(viewModel.isFavorite.value)
    }

    @Test
    fun `loadMovieDetails should handle repository errors gracefully`() = runTest {
        coEvery { movieRepository.getMovieDetails(any()) } throws Exception("Database error")

        viewModel.loadMovieDetails("123")

        assertNull(viewModel.movie.value)
        assertFalse(viewModel.isLoading.value)
    }
}
