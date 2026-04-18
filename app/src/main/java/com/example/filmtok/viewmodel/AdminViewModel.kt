package com.example.filmtok.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.filmtok.data.MovieRepository
import com.example.filmtok.data.StorageRepository
import com.example.filmtok.model.CastMember
import com.example.filmtok.model.Movie
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminMovieFormState(
    val title: String = "",
    val director: String = "",
    val year: String = "",
    val duration: String = "",
    val rating: String = "",
    val genre: String = "",
    val description: String = "",
    val posterUri: Uri? = null,
    val backdropUri: Uri? = null,
    val videoUri: Uri? = null,
    val existingPosterUrl: String = "",
    val existingBackdropUrl: String = "",
    val existingVideoUrl: String = "",
    val castMembers: List<CastMember> = emptyList(),
    val actorName: String = "",
    val actorRole: String = "",
    val actorImageUrl: String = ""
)

class AdminViewModel(
    private val repository: MovieRepository = MovieRepository(),
    private val storageRepository: StorageRepository = StorageRepository()
) : ViewModel() {
    private val _movies = MutableStateFlow<List<Movie>>(emptyList())
    val movies: StateFlow<List<Movie>> = _movies.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _uploadProgress = MutableStateFlow(0f)
    val uploadProgress: StateFlow<Float> = _uploadProgress.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess.asStateFlow()

    private val _uiState = MutableStateFlow(AdminMovieFormState())
    val uiState: StateFlow<AdminMovieFormState> = _uiState.asStateFlow()

    init {
        // Nie ładujemy filmów automatycznie w init, żeby nie blokować UI dodawania
    }

    fun loadMovies() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                _movies.value = repository.getAllMovies()
            } catch (e: Exception) {
                _errorMessage.value = "Nie można załadować filmów: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteMovie(movieId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.deleteMovie(movieId)
                loadMovies()
            } catch (e: Exception) {
                _errorMessage.value = "Błąd usuwania filmu: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadMovie(movieId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val movie = repository.getMovieDetails(movieId)

                if (movie != null) {
                    _uiState.update {
                        it.copy(
                            title = movie.title,
                            director = movie.director,
                            year = movie.year.toString(),
                            duration = movie.duration,
                            rating = movie.rating.toString(),
                            genre = movie.genres.joinToString(", "),
                            description = movie.description,
                            existingPosterUrl = movie.posterUrl,
                            existingBackdropUrl = movie.backdropUrl,
                            existingVideoUrl = movie.videoUrl,
                            castMembers = movie.cast
                        )
                    }
                } else {
                    _errorMessage.value = "Nie znaleziono filmu o podanym ID."
                }
            } catch (e: Exception) {
                _errorMessage.value = "Błąd ładowania filmu: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearMovieToEdit() {
        _uiState.value = AdminMovieFormState()
        _errorMessage.value = null
        _uploadProgress.value = 0f
    }

    fun onTitleChange(value: String) = _uiState.update { it.copy(title = value) }
    fun onDirectorChange(value: String) = _uiState.update { it.copy(director = value) }
    fun onYearChange(value: String) = _uiState.update { it.copy(year = value) }
    fun onDurationChange(value: String) = _uiState.update { it.copy(duration = value) }
    fun onRatingChange(value: String) = _uiState.update { it.copy(rating = value) }
    fun onGenreChange(value: String) = _uiState.update { it.copy(genre = value) }
    fun onDescriptionChange(value: String) = _uiState.update { it.copy(description = value) }
    fun onPosterUriChange(uri: Uri?) = _uiState.update { it.copy(posterUri = uri) }
    fun onBackdropUriChange(uri: Uri?) = _uiState.update { it.copy(backdropUri = uri) }
    fun onVideoUriChange(uri: Uri?) = _uiState.update { it.copy(videoUri = uri) }
    fun onActorNameChange(value: String) = _uiState.update { it.copy(actorName = value) }
    fun onActorRoleChange(value: String) = _uiState.update { it.copy(actorRole = value) }
    fun onActorImageUrlChange(value: String) = _uiState.update { it.copy(actorImageUrl = value) }

    fun addCastMember() {
        val state = _uiState.value
        if (state.actorName.isNotBlank() && state.actorRole.isNotBlank()) {
            val newMember = CastMember(
                name = state.actorName,
                character = state.actorRole,
                imageUrl = state.actorImageUrl
            )
            _uiState.update {
                it.copy(
                    castMembers = it.castMembers + newMember,
                    actorName = "",
                    actorRole = "",
                    actorImageUrl = ""
                )
            }
        }
    }

    fun removeCastMember(member: CastMember) {
        _uiState.update { it.copy(castMembers = it.castMembers - member) }
    }

    fun saveMovie(movieId: String?) {
        val state = _uiState.value
        if (state.title.isBlank() || state.director.isBlank()) return

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _uploadProgress.value = 0f
            try {
                val id = movieId ?: repository.getNewId()
                
                var posterUrl = state.existingPosterUrl
                if (state.posterUri != null) {
                    posterUrl = storageRepository.uploadImage(state.posterUri, "movies/$id/poster.jpg") {
                        _uploadProgress.value = it * 0.33f
                    }
                }

                var backdropUrl = state.existingBackdropUrl
                if (state.backdropUri != null) {
                    backdropUrl = storageRepository.uploadImage(state.backdropUri, "movies/$id/backdrop.jpg") {
                        _uploadProgress.value = 0.33f + (it * 0.33f)
                    }
                }

                var videoUrl = state.existingVideoUrl
                if (state.videoUri != null) {
                    videoUrl = storageRepository.uploadVideo(state.videoUri, "movies/$id/video.mp4") {
                        _uploadProgress.value = 0.66f + (it * 0.34f)
                    }
                }

                val movie = Movie(
                    id = id,
                    title = state.title,
                    director = state.director,
                    year = state.year.toIntOrNull() ?: 2024,
                    duration = state.duration,
                    rating = state.rating.toDoubleOrNull() ?: 0.0,
                    genres = state.genre.split(",").map { it.trim() },
                    description = state.description,
                    posterUrl = posterUrl,
                    backdropUrl = backdropUrl,
                    videoUrl = videoUrl,
                    cast = state.castMembers,
                    hasVideo = videoUrl.isNotEmpty()
                )
                repository.saveMovie(movie)
                _uploadProgress.value = 1f
                _saveSuccess.value = true
            } catch (e: Exception) {
                _errorMessage.value = "Błąd zapisu: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resetSuccess() {
        _saveSuccess.value = false
    }

    fun setErrorMessage(message: String?) {
        _errorMessage.value = message
    }
}
