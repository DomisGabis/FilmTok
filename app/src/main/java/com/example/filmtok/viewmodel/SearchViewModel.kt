package com.example.filmtok.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.filmtok.data.MovieRepository
import com.example.filmtok.data.StorageRepository
import com.example.filmtok.model.Movie
import com.example.filmtok.R
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
    
    val genres = listOf(
        R.string.genre_all,
        R.string.genre_sci_fi,
        R.string.genre_action,
        R.string.genre_drama,
        R.string.genre_comedy,
        R.string.genre_horror,
        R.string.genre_thriller
    )

    val filteredMovies: StateFlow<List<Movie>> = combine(_searchQuery, _selectedGenre, _allMovies) { query, genreRes, movies ->
        movies.filter { movie ->
            val matchesQuery = movie.title.contains(query, ignoreCase = true) || 
                             movie.director.contains(query, ignoreCase = true)
            
            // Note: This is a bit tricky since movie.genres are strings from DB.
            // For now, we compare against English strings or a mapping if needed.
            // Ideally, genres in DB should also be keys or we need a localized mapping.
            val matchesGenre = genreRes == R.string.genre_all || movie.genres.any { it.equals(getGenreName(genreRes), ignoreCase = true) }
            matchesQuery && matchesGenre
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private fun getGenreName(resId: Int): String {
        return when(resId) {
            R.string.genre_sci_fi -> "Sci-Fi"
            R.string.genre_action -> "Action"
            R.string.genre_drama -> "Drama"
            R.string.genre_comedy -> "Comedy"
            R.string.genre_horror -> "Horror"
            R.string.genre_thriller -> "Thriller"
            else -> ""
        }
    }

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
}
