package com.example.filmtok.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.filmtok.R
import com.example.filmtok.data.StorageRepository
import com.example.filmtok.data.UserRepository
import com.example.filmtok.model.Achievement
import com.example.filmtok.model.User
import com.example.filmtok.model.UserStats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val userRepository: UserRepository = UserRepository(),
    private val storageRepository: StorageRepository = StorageRepository()
) : ViewModel() {
    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading.asStateFlow()

    init {
        loadUserData()
    }

    private fun loadUserData() {
        val userId = userRepository.getCurrentUserId()
        if (userId != null) {
            // In a real app, you would fetch this from Firestore
            // For now, keeping the mock but pretending it's for the current user
            _user.value = User(
                id = userId,
                name = "Alex Cinemaphile",
                username = "alex_c",
                profileImageUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?q=80&w=1974&auto=format&fit=crop",
                stats = UserStats(
                    moviesInBase = 2,
                    averageRating = 4.7,
                    watchTimeHours = 5,
                    moviesWatched = 142
                ),
                favoriteGenres = listOf("Sci-Fi", "Action", "Drama"),
                achievements = listOf(
                    Achievement("1", R.string.achievement_collector_title, "Movie", R.string.achievement_collector_desc, 0xFFFF2D55, true),
                    Achievement("2", R.string.achievement_critic_title, "Star", R.string.achievement_critic_desc, 0xFF00BFFF, true),
                    Achievement("3", R.string.achievement_marathoner_title, "Timer", R.string.achievement_marathoner_desc, 0xFF8A2BE2, false)
                )
            )
        }
    }

    fun updateProfilePicture(uri: Uri) {
        val userId = userRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            try {
                _isUploading.value = true
                val path = "profile_pics/$userId.jpg"
                val url = storageRepository.uploadImage(uri, path)
                userRepository.updateProfile(userId, mapOf("profileImageUrl" to url))
                _user.value = _user.value?.copy(profileImageUrl = url)
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isUploading.value = false
            }
        }
    }

    fun updateUsername(newUsername: String) {
        val userId = userRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            try {
                userRepository.updateProfile(userId, mapOf("username" to newUsername))
                _user.value = _user.value?.copy(username = newUsername)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}
