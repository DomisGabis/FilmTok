package com.example.filmtok.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.filmtok.data.MovieRepository
import com.example.filmtok.data.StorageRepository
import com.example.filmtok.data.UserRepository
import com.example.filmtok.model.Movie
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MovieDetailsViewModel(
    private val repository: MovieRepository = MovieRepository(),
    private val storageRepository: StorageRepository = StorageRepository(),
    private val userRepository: UserRepository = UserRepository()
) : ViewModel() {
    private val _movie = MutableStateFlow<Movie?>(null)
    val movie: StateFlow<Movie?> = _movie.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow()

    fun loadMovieDetails(movieId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val movieData = repository.getMovieDetails(movieId)
                _movie.value = movieData?.let { storageRepository.resolveMovieUrls(it) }
                _isFavorite.value = userRepository.isMovieFavorite(movieId)
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleFavorite() {
        val currentMovie = _movie.value ?: return
        viewModelScope.launch {
            try {
                if (_isFavorite.value) {
                    userRepository.removeFavoriteMovie(currentMovie.id)
                    _isFavorite.value = false
                } else {
                    userRepository.addFavoriteMovie(currentMovie.id)
                    _isFavorite.value = true
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}
