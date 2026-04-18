package com.example.filmtok.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.filmtok.data.MovieRepository
import com.example.filmtok.data.StorageRepository
import com.example.filmtok.model.Movie
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SearchViewModel(
    private val repository: MovieRepository = MovieRepository(),
    private val storageRepository: StorageRepository = StorageRepository()
) : ViewModel() {
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedGenre = MutableStateFlow("Wszystkie")
    val selectedGenre: StateFlow<String> = _selectedGenre.asStateFlow()

    private val _allMovies = MutableStateFlow<List<Movie>>(emptyList())
    
    val genres = listOf("Wszystkie", "Sci-Fi", "Action", "Drama", "Comedy", "Horror", "Thriller")

    val filteredMovies: StateFlow<List<Movie>> = combine(_searchQuery, _selectedGenre, _allMovies) { query, genre, movies ->
        movies.filter { movie ->
            val matchesQuery = movie.title.contains(query, ignoreCase = true) || 
                             movie.director.contains(query, ignoreCase = true)
            val matchesGenre = genre == "Wszystkie" || movie.genres.contains(genre)
            matchesQuery && matchesGenre
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadMovies()
    }

    private fun loadMovies() {
        viewModelScope.launch {
            try {
                val movies = repository.getAllMovies()
                _allMovies.value = movies.map { storageRepository.resolveMovieUrls(it) }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun onGenreSelect(genre: String) {
        _selectedGenre.value = genre
    }
}
