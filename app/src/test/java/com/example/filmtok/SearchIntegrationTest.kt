package com.example.filmtok

import com.example.filmtok.data.MovieRepository
import com.example.filmtok.data.StorageRepository
import com.example.filmtok.model.Movie
import com.example.filmtok.model.MovieGenre
import com.example.filmtok.viewmodel.SearchViewModel
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
@OptIn(ExperimentalCoroutinesApi::class)
class SearchIntegrationTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val movieRepository = mockk<MovieRepository>(relaxed = true)
    private val storageRepository = mockk<StorageRepository>(relaxed = true)
    private lateinit var searchViewModel: SearchViewModel

    private val testMovies = listOf(
        Movie(id = "1", title = "The Dark Knight", director = "Christopher Nolan", genres = listOf(MovieGenre.ACTION)),
        Movie(id = "2", title = "Inception", director = "Christopher Nolan", genres = listOf(MovieGenre.SCI_FI)),
        Movie(id = "3", title = "Pulp Fiction", director = "Quentin Tarantino", genres = listOf(MovieGenre.CRIME))
    )

    @Before
    fun setup() {
        // Symulujemy, że repozytorium zwraca listę filmów
        coEvery { movieRepository.getAllMovies() } returns testMovies
        // StorageRepository po prostu przepuszcza filmy dalej
        coEvery { storageRepository.resolveMovieUrls(any()) } answers { firstArg() }
        
        searchViewModel = SearchViewModel(movieRepository, storageRepository)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `integration - searching for Nolan returns only his movies`() = runTest {
        // Aktywujemy StateFlow poprzez uruchomienie zbierania danych w tle
        val job = backgroundScope.launch { searchViewModel.filteredMovies.collect() }
        
        // Czekamy na wykonanie init i loadMovies()
        advanceUntilIdle()

        // Act - Zmieniamy frazę wyszukiwania
        searchViewModel.onSearchQueryChange("Nolan")
        advanceUntilIdle() // Czekamy na przetworzenie zmiany przez Flow

        // Assert - Sprawdzamy stan pofiltrowanej listy w ViewModelu
        val filtered = searchViewModel.filteredMovies.value
        assertEquals(2, filtered.size)
        assertEquals("The Dark Knight", filtered[0].title)
        assertEquals("Inception", filtered[1].title)
    }

    @Test
    fun `integration - filtering by Sci-Fi returns only Sci-Fi movies`() = runTest {
        // Aktywujemy StateFlow
        val job = backgroundScope.launch { searchViewModel.filteredMovies.collect() }
        
        // Czekamy na wykonanie init i loadMovies()
        advanceUntilIdle()

        // Act - Wybieramy gatunek Sci-Fi
        searchViewModel.onGenreSelect(MovieGenre.SCI_FI.labelRes)
        advanceUntilIdle()

        // Assert
        val filtered = searchViewModel.filteredMovies.value
        assertEquals(1, filtered.size)
        assertEquals("Inception", filtered[0].title)
    }
}
