package com.example.filmtok

import android.net.Uri
import com.example.filmtok.data.MovieRepository
import com.example.filmtok.data.StorageRepository
import com.example.filmtok.model.MovieGenre
import com.example.filmtok.viewmodel.AdminViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class AdminValidationTest {

    private lateinit var viewModel: AdminViewModel

    // Tworzymy makiety repozytoriów
    private val movieRepository = mockk<MovieRepository>(relaxed = true)
    private val storageRepository = mockk<StorageRepository>(relaxed = true)

    @Before
    fun setup() {
        // Mockujemy statyczne metody klasy Uri, aby nie rzucały błędów w środowisku JVM
        mockkStatic(Uri::class)
        every { Uri.parse(any()) } returns mockk(relaxed = true)

        // Przekazujemy zmockowane repozytoria jawnym konstruktorem.
        // Dzięki temu unikamy wywołania domyślnego konstruktora, który próbowałby inicjalizować Firebase.
        viewModel = AdminViewModel(
            repository = movieRepository,
            storageRepository = storageRepository
        )
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `initial state should be empty`() {
        val state = viewModel.uiState.value
        assertTrue(state.title.isEmpty())
        assertTrue(state.director.isEmpty())
        assertTrue(state.genres.isEmpty())
        assertTrue(state.castMembers.isEmpty())
    }

    @Test
    fun `onTitleChange should update title in state`() {
        val testTitle = "Inception"
        viewModel.onTitleChange(testTitle)
        assertEquals(testTitle, viewModel.uiState.value.title)
    }

    @Test
    fun `onGenreToggle should add genre if not present`() {
        val genre = MovieGenre.ACTION
        viewModel.onGenreToggle(genre)
        assertTrue(viewModel.uiState.value.genres.contains(genre))
    }

    @Test
    fun `onGenreToggle should remove genre if already present`() {
        val genre = MovieGenre.DRAMA
        viewModel.onGenreToggle(genre) // Dodaj
        viewModel.onGenreToggle(genre) // Usuń
        assertFalse(viewModel.uiState.value.genres.contains(genre))
    }

    @Test
    fun `addCastMember should add member to list and clear temporary fields`() {
        viewModel.onActorNameChange("Leonardo DiCaprio")
        viewModel.onActorRoleChange("Cobb")

        viewModel.addCastMember()

        val state = viewModel.uiState.value
        assertEquals(1, state.castMembers.size)
        assertEquals("Leonardo DiCaprio", state.castMembers[0].name)
        assertEquals("Cobb", state.castMembers[0].character)

        // Pola tymczasowe powinny zostać wyczyszczone
        assertTrue(state.actorName.isEmpty())
        assertTrue(state.actorRole.isEmpty())
    }

    @Test
    fun `addCastMember should not add member if name or role is blank`() {
        viewModel.onActorNameChange("")
        viewModel.onActorRoleChange("Some Role")
        viewModel.addCastMember()
        assertTrue(viewModel.uiState.value.castMembers.isEmpty())

        viewModel.onActorNameChange("Some Actor")
        viewModel.onActorRoleChange("")
        viewModel.addCastMember()
        assertTrue(viewModel.uiState.value.castMembers.isEmpty())
    }

    @Test
    fun `removeCastMember should remove specific member from the list`() {
        // Najpierw dodaj członka
        viewModel.onActorNameChange("Actor to remove")
        viewModel.onActorRoleChange("Role")
        viewModel.addCastMember()

        val member = viewModel.uiState.value.castMembers[0]
        assertEquals(1, viewModel.uiState.value.castMembers.size)

        // Usuń go
        viewModel.removeCastMember(member)
        assertTrue(viewModel.uiState.value.castMembers.isEmpty())
    }

    @Test
    fun `clearMovieToEdit should reset all form fields`() {
        viewModel.onTitleChange("Sample Title")
        viewModel.onDirectorChange("Sample Director")
        viewModel.onGenreToggle(MovieGenre.COMEDY)

        viewModel.clearMovieToEdit()

        val state = viewModel.uiState.value
        assertTrue(state.title.isEmpty())
        assertTrue(state.director.isEmpty())
        assertTrue(state.genres.isEmpty())
    }

    @Test
    fun `setErrorMessage should update error message state`() {
        val error = "Validation error"
        viewModel.setErrorMessage(error)
        assertEquals(error, viewModel.errorMessage.value)
    }
}
