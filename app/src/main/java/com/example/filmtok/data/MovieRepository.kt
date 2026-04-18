package com.example.filmtok.data

import com.example.filmtok.model.Movie
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class MovieRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val moviesCollection = firestore.collection("movies")

    suspend fun getHeroMovie(): Movie? {
        return try {
            moviesCollection
                .whereEqualTo("isHero", true)
                .limit(1)
                .get()
                .await()
                .toObjects(Movie::class.java)
                .firstOrNull()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getRecentlyWatched(): List<Movie> {
        return try {
            moviesCollection
                .limit(10)
                .get()
                .await()
                .toObjects(Movie::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getMovieDetails(movieId: String): Movie? {
        return try {
            moviesCollection
                .document(movieId)
                .get()
                .await()
                .toObject(Movie::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getReels(): List<Movie> {
        return try {
            moviesCollection
                .whereEqualTo("hasVideo", true)
                .get()
                .await()
                .toObjects(Movie::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getAllMovies(): List<Movie> {
        return try {
            moviesCollection
                .get()
                .await()
                .toObjects(Movie::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun deleteMovie(movieId: String) {
        moviesCollection.document(movieId).delete().await()
    }

    suspend fun saveMovie(movie: Movie) {
        val docRef = if (movie.id.isEmpty()) {
            moviesCollection.document()
        } else {
            moviesCollection.document(movie.id)
        }
        val movieToSave = if (movie.id.isEmpty()) movie.copy(id = docRef.id) else movie
        docRef.set(movieToSave).await()
    }
}