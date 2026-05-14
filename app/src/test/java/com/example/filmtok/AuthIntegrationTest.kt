package com.example.filmtok

import com.example.filmtok.data.UserRepository
import com.example.filmtok.viewmodel.AuthViewModel
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class AuthIntegrationTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val mockAuth = mockk<FirebaseAuth>(relaxed = true)
    private val mockDb = mockk<FirebaseFirestore>(relaxed = true)
    private val mockUser = mockk<FirebaseUser>(relaxed = true)
    private val mockDoc = mockk<DocumentSnapshot>(relaxed = true)

    private lateinit var userRepository: UserRepository
    private lateinit var authViewModel: AuthViewModel

    @Before
    fun setup() {
        mockkStatic(FirebaseAuth::class)
        every { FirebaseAuth.getInstance() } returns mockAuth
        
        mockkStatic(FirebaseFirestore::class)
        every { FirebaseFirestore.getInstance() } returns mockDb

        userRepository = UserRepository(mockAuth, mockDb)
        authViewModel = AuthViewModel(userRepository)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `login flow integration - successful sign in updates role to admin`() = runTest {
        val email = "admin@filmtok.pl"
        val password = "securePassword"
        val uid = "admin_uid_123"

        val mockAuthResult = mockk<AuthResult>()
        every { mockUser.uid } returns uid
        every { mockAuth.signInWithEmailAndPassword(email, password) } returns Tasks.forResult(mockAuthResult)
        every { mockAuthResult.user } returns mockUser

        every { mockDb.collection("users").document(uid).get() } returns Tasks.forResult(mockDoc)
        every { mockDoc.exists() } returns true
        every { mockDoc.getBoolean("isAdmin") } returns true

        authViewModel.signIn(email, password)
        advanceUntilIdle()

        assertEquals("admin", authViewModel.userRole.value)
    }

    @Test
    fun `logout flow integration - signing out clears user state in ViewModel`() = runTest {
        every { mockDb.collection("users").document(any()).get() } returns Tasks.forResult(mockDoc)
        every { mockDoc.exists() } returns true
        every { mockDoc.getBoolean("isAdmin") } returns true
        
        authViewModel.checkUserRole("some_uid")
        advanceUntilIdle()
        
        assertEquals("admin", authViewModel.userRole.value)

        authViewModel.signOut()

        assertEquals(null, authViewModel.userRole.value)
        verify { mockAuth.signOut() }
    }
}
