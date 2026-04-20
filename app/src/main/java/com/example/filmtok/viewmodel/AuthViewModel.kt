package com.example.filmtok.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.filmtok.data.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repository: UserRepository = UserRepository()
) : ViewModel() {

    private val _userRole = MutableStateFlow<String?>(null)
    val userRole: StateFlow<String?> = _userRole.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        currentUser?.let {
            checkUserRole(it.uid)
        }
    }

    fun checkUserRole(uid: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _userRole.value = repository.fetchOrCreateUserRole(uid)
            } catch (e: Exception) {
                _errorMessage.value = e.message
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
                repository.registerWithEmail(email, pass)
                _userRole.value = "user"
            } catch (e: Exception) {
                _errorMessage.value = e.message
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
                val uid = repository.signInWithEmail(email, pass)
                checkUserRole(uid)
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun signOut() {
        repository.signOut()
        _userRole.value = null
    }

    fun setErrorMessage(message: String?) {
        _errorMessage.value = message
    }
}