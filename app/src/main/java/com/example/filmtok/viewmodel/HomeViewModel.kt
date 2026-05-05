package com.example.filmtok.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.filmtok.data.MovieRepository
import com.example.filmtok.data.StorageRepository
import com.example.filmtok.model.Movie
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
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
        observeMovies()
    }

    private fun observeMovies() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getMoviesFlow().collect { movies ->
                try {
                    // Wybór filmu dnia (isHero lub pierwszy z listy)
                    val hero = movies.find { it.isHero } ?: movies.firstOrNull()
                    _heroMovie.value = hero?.let { storageRepository.resolveMovieUrls(it) }

                    // Rozwiązanie URLi dla listy filmów (np. 10 ostatnich)
                    val resolvedRecently = movies.take(10).map { movie ->
                        async { storageRepository.resolveMovieUrls(movie) }
                    }.awaitAll()

                    _recentlyWatched.value = resolvedRecently
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    _isLoading.value = false
                }
            }
        }
    }

    fun fetchHomeData() {
        observeMovies()
    }
}
