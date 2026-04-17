package com.example.filmtok.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.filmtok.data.repository.MovieRepository
import com.example.filmtok.model.Movie
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    private val repository = MovieRepository()

    private val _heroMovie = MutableStateFlow<Movie?>(null)
    val heroMovie: StateFlow<Movie?> = _heroMovie.asStateFlow()

    private val _recentlyWatched = MutableStateFlow<List<Movie>>(emptyList())
    val recentlyWatched: StateFlow<List<Movie>> = _recentlyWatched.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        fetchHomeData()
    }

    fun fetchHomeData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _heroMovie.value = repository.getHeroMovie()
                _recentlyWatched.value = repository.getRecentlyWatched()
            } catch (e: Exception) {
                // Obsługa błędów w realnej aplikacji
            } finally {
                _isLoading.value = false
            }
        }
    }
}
