package com.example.filmtok.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.filmtok.data.MovieRepository
import com.example.filmtok.data.StorageRepository
import com.example.filmtok.model.Movie
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: MovieRepository = MovieRepository(),
    private val storageRepository: StorageRepository = StorageRepository()
) : ViewModel() {

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
                val hero = repository.getHeroMovie()
                val recently = repository.getRecentlyWatched()
                
                _heroMovie.value = hero?.let { storageRepository.resolveMovieUrls(it) }
                _recentlyWatched.value = recently.map { storageRepository.resolveMovieUrls(it) }
            } catch (e: Exception) {
                // Obsługa błędów
            } finally {
                _isLoading.value = false
            }
        }
    }
}
