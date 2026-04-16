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
                val user = document.toObject(User::class.java)
                if (user?.isAdmin == true) {
                    _userRole.value = "admin"
                } else {
                    _userRole.value = "user"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Błąd pobierania roli: ${e.message}"
                _userRole.value = "user" // Default to user on error
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun signOut() {
        auth.signOut()
        _userRole.value = null
    }
}
