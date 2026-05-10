package com.example.filmtok

import com.example.filmtok.data.UserRepository
import com.example.filmtok.viewmodel.AuthViewModel
import com.google.firebase.auth.FirebaseAuth
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthLogicTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: AuthViewModel
    private val userRepository = mockk<UserRepository>(relaxed = true)
    private val firebaseAuth = mockk<FirebaseAuth>(relaxed = true)

    @Before
    fun setup() {
        mockkStatic(FirebaseAuth::class)
        every { FirebaseAuth.getInstance() } returns firebaseAuth
        every { firebaseAuth.currentUser } returns null

        viewModel = AuthViewModel(userRepository)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `checkUserRole should update userRole state on success`() = runTest {
        val testUid = "user123"
        val expectedRole = "admin"
        coEvery { userRepository.fetchOrCreateUserRole(testUid) } returns expectedRole

        viewModel.checkUserRole(testUid)

        assertEquals(expectedRole, viewModel.userRole.value)
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `checkUserRole should set default role and error message on failure`() = runTest {
        val testUid = "user123"
        val errorMsg = "Network error"
        coEvery { userRepository.fetchOrCreateUserRole(testUid) } throws Exception(errorMsg)

        viewModel.checkUserRole(testUid)

        assertEquals("user", viewModel.userRole.value)
        assertEquals(errorMsg, viewModel.errorMessage.value)
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `signIn should trigger role check on success`() = runTest {
        val email = "test@example.com"
        val pass = "password"
        val uid = "new_uid"

        coEvery { userRepository.signInWithEmail(email, pass) } returns uid
        coEvery { userRepository.fetchOrCreateUserRole(uid) } returns "user"

        viewModel.signIn(email, pass)

        coVerify { userRepository.signInWithEmail(email, pass) }
        coVerify { userRepository.fetchOrCreateUserRole(uid) }
        assertEquals("user", viewModel.userRole.value)
    }

    @Test
    fun `register should update userRole to user on success`() = runTest {
        val email = "test@example.com"
        val pass = "password"

        coEvery { userRepository.registerWithEmail(email, pass) } returns "uid"

        viewModel.register(email, pass)

        assertEquals("user", viewModel.userRole.value)
        assertNull(viewModel.errorMessage.value)
    }

    @Test
    fun `signOut should clear userRole state`() = runTest {
        coEvery { userRepository.fetchOrCreateUserRole(any()) } returns "admin"
        viewModel.checkUserRole("123")

        viewModel.signOut()

        assertNull(viewModel.userRole.value)
        verify { userRepository.signOut() }
    }

    @Test
    fun `setErrorMessage should update state correctly`() {
        val error = "Custom error"
        viewModel.setErrorMessage(error)
        assertEquals(error, viewModel.errorMessage.value)
    }
}
