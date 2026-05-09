package com.example.filmtok.viewmodel

import android.net.Uri
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.filmtok.R
import com.example.filmtok.data.MovieRepository
import com.example.filmtok.data.StorageRepository
import com.example.filmtok.data.UserRepository
import com.example.filmtok.model.Movie
import com.example.filmtok.model.User
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val userRepository: UserRepository = UserRepository(),
    private val storageRepository: StorageRepository = StorageRepository(),
    private val movieRepository: MovieRepository = MovieRepository()
) : ViewModel() {
    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    private val _favoriteMovies = MutableStateFlow<List<Movie>>(emptyList())
    val favoriteMovies: StateFlow<List<Movie>> = _favoriteMovies.asStateFlow()

    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading.asStateFlow()

    init {
        loadUserData()
    }

    private fun loadUserData() {
        viewModelScope.launch {
            val userId = userRepository.getCurrentUserId()
            val email = userRepository.getCurrentUserEmail() ?: ""
            val profileImageUrl = userRepository.getCurrentUserProfileImage() ?: storageRepository.getDefaultUserImageUrl()
            val favoriteMovieIds = userRepository.getCurrentUserFavoriteMovies()
            val favoriteGenres = userRepository.getCurrentUserTop3FavoriteGenres()

            if (userId != null) {
                _user.value = User(
                    id = userId,
                    email = email,
                    profileImageUrl = profileImageUrl,
                    favoriteMovies = favoriteMovieIds,
                    favoriteGenres = favoriteGenres
                )

                // Fetch movie details for favorites
                val movies = favoriteMovieIds.map { id ->
                    async { movieRepository.getMovieDetails(id) }
                }.awaitAll().filterNotNull()
                _favoriteMovies.value = movies
            }
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
}
