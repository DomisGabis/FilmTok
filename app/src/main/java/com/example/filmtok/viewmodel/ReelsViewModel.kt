package com.example.filmtok.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.filmtok.data.MovieRepository
import com.example.filmtok.model.Movie
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ReelsViewModel(private val repository: MovieRepository = MovieRepository()) : ViewModel() {
    private val _reels = MutableStateFlow<List<Movie>>(emptyList())
    val reels: StateFlow<List<Movie>> = _reels.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadReels()
    }

    private fun loadReels() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _reels.value = repository.getReels()
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }
}
