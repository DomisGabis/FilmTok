package com.example.filmtok

import android.net.Uri
import com.example.filmtok.data.MovieRepository
import com.example.filmtok.data.StorageRepository
import com.example.filmtok.data.UserRepository
import com.example.filmtok.model.Movie
import com.example.filmtok.viewmodel.ProfileViewModel
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
@OptIn(ExperimentalCoroutinesApi::class)
class ProfileIntegrationTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val userRepository = mockk<UserRepository>(relaxed = true)
    private val storageRepository = mockk<StorageRepository>(relaxed = true)
    private val movieRepository = mockk<MovieRepository>(relaxed = true)
    private lateinit var viewModel: ProfileViewModel

    @Before
    fun setup() {
        mockkStatic(Uri::class)
        val mockUri = mockk<Uri>()
        every { Uri.parse(any()) } returns mockUri
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `integration - loading profile fetches user data and favorite movies details`() = runTest {
        val userId = "test_user"
        val favoriteIds = listOf("m1", "m2")
        val movie1 = Movie(id = "m1", title = "Movie 1")
        val movie2 = Movie(id = "m2", title = "Movie 2")

        every { userRepository.getCurrentUserId() } returns userId
        every { userRepository.getCurrentUserEmail() } returns "user@test.com"
        coEvery { userRepository.getCurrentUserFavoriteMovies() } returns favoriteIds
        
        coEvery { movieRepository.getMovieDetails("m1") } returns movie1
        coEvery { movieRepository.getMovieDetails("m2") } returns movie2

        viewModel = ProfileViewModel(userRepository, storageRepository, movieRepository)

        assertEquals("user@test.com", viewModel.user.value?.email)

        assertEquals(2, viewModel.favoriteMovies.value.size)
        assertEquals("Movie 1", viewModel.favoriteMovies.value[0].title)
        assertEquals("Movie 2", viewModel.favoriteMovies.value[1].title)
    }

    @Test
    fun `integration - updateProfilePicture uploads image and updates user model`() = runTest {
        val userId = "test_user"
        val newImageUrl = "https://firebasestorage.../new_avatar.jpg"
        val mockUri = mockk<Uri>()
        
        every { userRepository.getCurrentUserId() } returns userId
        coEvery { storageRepository.uploadImage(mockUri, any()) } returns newImageUrl
        
        viewModel = ProfileViewModel(userRepository, storageRepository, movieRepository)

        viewModel.updateProfilePicture(mockUri)

        coVerify { userRepository.updateProfile(userId, mapOf("profileImageUrl" to newImageUrl)) }

        assertEquals(newImageUrl, viewModel.user.value?.profileImageUrl)
    }
}
