package com.example.filmtok

import com.example.filmtok.data.MovieRepository
import com.example.filmtok.data.StorageRepository
import com.example.filmtok.model.Movie
import com.example.filmtok.viewmodel.HomeViewModel
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
@OptIn(ExperimentalCoroutinesApi::class)
class HomeIntegrationTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val movieRepository = mockk<MovieRepository>(relaxed = true)
    private val storageRepository = mockk<StorageRepository>(relaxed = true)
    private lateinit var homeViewModel: HomeViewModel

    @Before
    fun setup() {
        // ViewModel przy starcie wywołuje observeMovies(), więc musimy przygotować mocki wcześniej
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `integration - home screen loads movies and resolves their urls`() = runTest {
        // 1. Arrange
        val rawMovie = Movie(id = "1", title = "Raw Movie", posterUrl = "path/to/poster")
        val resolvedMovie = rawMovie.copy(posterUrl = "https://url.to/poster")
        
        // Symulujemy, że baza zwraca strumień z jednym filmem
        every { movieRepository.getMoviesFlow() } returns flowOf(listOf(rawMovie))
        
        // Symulujemy, że StorageRepository zamienia ścieżkę na URL
        coEvery { storageRepository.resolveMovieUrls(rawMovie) } returns resolvedMovie

        // 2. Act - Tworzymy ViewModel (co wyzwala init i observeMovies)
        homeViewModel = HomeViewModel(movieRepository, storageRepository)

        // 3. Assert
        // Sprawdzamy, czy film w ViewModelu jest tym "rozwiązanym" (z pełnym adresem URL)
        assertEquals("https://url.to/poster", homeViewModel.heroMovie.value?.posterUrl)
        
        // Sprawdzamy, czy film trafił na listę "Recently Watched"
        assertEquals(1, homeViewModel.recentlyWatched.value.size)
        assertEquals("https://url.to/poster", homeViewModel.recentlyWatched.value[0].posterUrl)
        
        // Sprawdzamy, czy stan ładowania został wyłączony
        assertFalse(homeViewModel.isLoading.value)
    }

    @Test
    fun `integration - hero movie is selected correctly from list`() = runTest {
        // 1. Arrange
        val movie1 = Movie(id = "1", title = "Regular", isHero = false)
        val movie2 = Movie(id = "2", title = "The Hero", isHero = true)
        
        every { movieRepository.getMoviesFlow() } returns flowOf(listOf(movie1, movie2))
        coEvery { storageRepository.resolveMovieUrls(any()) } answers { firstArg() }

        // 2. Act
        homeViewModel = HomeViewModel(movieRepository, storageRepository)

        // 3. Assert
        // ViewModel powinien znaleźć film z flagą isHero = true
        assertEquals("2", homeViewModel.heroMovie.value?.id)
        assertEquals("The Hero", homeViewModel.heroMovie.value?.title)
    }
}
