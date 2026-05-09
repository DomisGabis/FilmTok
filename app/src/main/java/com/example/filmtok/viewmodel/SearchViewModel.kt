package com.example.filmtok.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.filmtok.data.MovieRepository
import com.example.filmtok.data.StorageRepository
import com.example.filmtok.model.Movie
import com.example.filmtok.R
import com.example.filmtok.model.MovieGenre
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SearchViewModel(
    private val repository: MovieRepository = MovieRepository(),
    private val storageRepository: StorageRepository = StorageRepository()
) : ViewModel() {
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedGenre = MutableStateFlow(R.string.genre_all)
    val selectedGenre: StateFlow<Int> = _selectedGenre.asStateFlow()

    private val _allMovies = MutableStateFlow<List<Movie>>(emptyList())
    
    val genres = MovieGenre.entries.map { it.labelRes }

    val filteredMovies: StateFlow<List<Movie>> = combine(_searchQuery, _selectedGenre, _allMovies) { query, genreRes, movies ->
        filterMovies(movies, query, genreRes)
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

    fun onGenreSelect(genreRes: Int) {
        _selectedGenre.value = genreRes
    }

    companion object {
        fun filterMovies(movies: List<Movie>, query: String, genreResId: Int): List<Movie> {
            return movies.filter { movie ->
                val matchesQuery = movie.title.contains(query, ignoreCase = true) || 
                                 movie.director.contains(query, ignoreCase = true)
                
                val matchesGenre = genreResId == R.string.genre_all || movie.genres.any { it.labelRes == genreResId }
                matchesQuery && matchesGenre
            }
        }
    }
}
