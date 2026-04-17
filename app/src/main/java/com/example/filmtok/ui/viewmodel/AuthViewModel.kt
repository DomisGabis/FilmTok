package com.example.filmtok.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.filmtok.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _userRole = MutableStateFlow<String?>(null) // "admin", "user", or null
    val userRole: StateFlow<String?> = _userRole.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun checkUserRole(uid: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val document = db.collection("users").document(uid).get().await()
                if (document.exists()) {
                    val isAdmin = document.getBoolean("isAdmin") ?: false
                    _userRole.value = if (isAdmin) "admin" else "user"
                } else {
                    // Jeśli dokumentu nie ma, stwórzmy go jako zwykły użytkownik
                    val newUser = mapOf("uid" to uid, "isAdmin" to false, "email" to auth.currentUser?.email)
                    db.collection("users").document(uid).set(newUser).await()
                    _userRole.value = "user"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Błąd pobierania roli: ${e.message}"
                _userRole.value = "user"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun register(email: String, pass: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val result = auth.createUserWithEmailAndPassword(email, pass).await()
                val uid = result.user?.uid ?: return@launch
                
                // Tworzymy dokument w Firestore dla nowego usera
                val userMetadata = mapOf(
                    "uid" to uid,
                    "email" to email,
                    "isAdmin" to false // Domyślnie każdy jest zwykłym userem
                )
                db.collection("users").document(uid).set(userMetadata).await()
                
                _userRole.value = "user"
            } catch (e: Exception) {
                _errorMessage.value = "Błąd rejestracji: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun signIn(email: String, pass: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val result = auth.signInWithEmailAndPassword(email, pass).await()
                val uid = result.user?.uid ?: return@launch
                checkUserRole(uid)
            } catch (e: Exception) {
                _errorMessage.value = "Błąd logowania: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun signOut() {
        auth.signOut()
        _userRole.value = null
    }

    fun setErrorMessage(message: String?) {
        _errorMessage.value = message
    }
}
