package com.example.filmtok

import android.net.Uri
import com.example.filmtok.data.MovieRepository
import com.example.filmtok.data.StorageRepository
import com.example.filmtok.model.Movie
import com.example.filmtok.model.MovieGenre
import com.example.filmtok.viewmodel.AdminViewModel
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
@OptIn(ExperimentalCoroutinesApi::class)
class AdminMovieIntegrationTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val movieRepository = mockk<MovieRepository>(relaxed = true)
    private val storageRepository = mockk<StorageRepository>(relaxed = true)
    private lateinit var viewModel: AdminViewModel

    @Before
    fun setup() {
        // Mockowanie Uri.parse, aby uniknąć błędów JVM
        mockkStatic(Uri::class)
        val mockUri = mockk<Uri>()
        every { Uri.parse(any()) } returns mockUri

        viewModel = AdminViewModel(movieRepository, storageRepository)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `integration - saving a new movie uploads files and saves document`() = runTest {
        // 1. Arrange - wypełniamy formularz w ViewModelu
        val testTitle = "Interstellar"
        viewModel.onTitleChange(testTitle)
        viewModel.onDirectorChange("Christopher Nolan")
        viewModel.onYearChange("2014")
        viewModel.onGenreToggle(MovieGenre.SCI_FI)
        
        // Symulujemy wybrane pliki
        val posterUri = mockk<Uri>()
        viewModel.onPosterUriChange(posterUri)

        // Mockowanie zachowania repozytoriów
        val generatedId = "new_movie_id"
        val uploadedUrl = "https://firebasestorage.../poster.jpg"
        
        every { movieRepository.getNewId() } returns generatedId
        coEvery { storageRepository.uploadImage(posterUri, any(), any()) } returns uploadedUrl

        // 2. Act - wywołujemy zapis
        viewModel.saveMovie(null) // null oznacza nowy film

        // 3. Assert - weryfikujemy integrację całego flow
        
        // Czy plik został wysłany do Storage?
        coVerify { storageRepository.uploadImage(posterUri, match { it.contains(generatedId) }, any()) }
        
        // Czy film został zapisany w bazie z poprawnym adresem URL ze Storage?
        val movieSlot = slot<Movie>()
        coVerify { movieRepository.saveMovie(capture(movieSlot)) }
        
        val savedMovie = movieSlot.captured
        assertTrue("Tytuł powinien się zgadzać", savedMovie.title == testTitle)
        assertTrue("URL plakatu powinien pochodzić ze Storage", savedMovie.posterUrl == uploadedUrl)
        assertTrue("Stan sukcesu powinien być ustawiony", viewModel.saveSuccess.value)
    }
}
