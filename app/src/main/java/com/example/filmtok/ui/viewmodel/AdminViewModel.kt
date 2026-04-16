package com.example.filmtok.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.filmtok.data.repository.MovieRepository
import com.example.filmtok.model.Movie
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AdminViewModel(private val repository: MovieRepository = MovieRepository()) : ViewModel() {
    private val _movies = MutableStateFlow<List<Movie>>(emptyList())
    val movies: StateFlow<List<Movie>> = _movies.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess.asStateFlow()

    init {
        loadMovies()
    }

    fun loadMovies() {
        viewModelScope.launch {
            _movies.value = repository.getAllMovies()
        }
    }

    fun deleteMovie(movieId: String) {
        viewModelScope.launch {
            repository.deleteMovie(movieId)
            loadMovies()
        }
    }

    private val _movieToEdit = MutableStateFlow<Movie?>(null)
    val movieToEdit: StateFlow<Movie?> = _movieToEdit.asStateFlow()

    fun loadMovie(movieId: String) {
        viewModelScope.launch {
            _movieToEdit.value = repository.getMovieDetails(movieId)
        }
    }

    fun clearMovieToEdit() {
        _movieToEdit.value = null
    }

    fun saveMovie(movie: Movie) {
        viewModelScope.launch {
            _isSaving.value = true
            try {
                repository.saveMovie(movie)
                _saveSuccess.value = true
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isSaving.value = false
            }
        }
    }
    
    fun resetSuccess() {
        _saveSuccess.value = false
    }
}
