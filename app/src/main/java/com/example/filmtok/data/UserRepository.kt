package com.example.filmtok.data

import com.example.filmtok.model.MovieGenre
import com.example.filmtok.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

class UserRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    suspend fun fetchOrCreateUserRole(uid: String): String {
        val document = db.collection("users").document(uid).get().await()

        return if (document.exists()) {
            val isAdmin = document.getBoolean("isAdmin") ?: false
            if (isAdmin) "admin" else "user"
        } else {
            val newUser = mapOf(
                "uid" to uid,
                "isAdmin" to false,
                "email" to auth.currentUser?.email
            )
            db.collection("users").document(uid).set(newUser).await()
            "user"
        }
    }

    suspend fun createUserInFirestore(uid: String, email: String) {
        val userMetadata = mapOf(
            "uid" to uid,
            "email" to email,
            "isAdmin" to false
        )
        db.collection("users").document(uid).set(userMetadata).await()
    }

    suspend fun registerWithEmail(email: String, pass: String): String {
        val result = auth.createUserWithEmailAndPassword(email, pass).await()
        val uid = result.user?.uid ?: error("Brak UID po rejestracji")
        createUserInFirestore(uid, email)
        return uid
    }

    suspend fun signInWithEmail(email: String, pass: String): String {
        val result = auth.signInWithEmailAndPassword(email, pass).await()
        return result.user?.uid ?: error("Brak UID po logowaniu")
    }

    suspend fun updateProfile(uid: String, updates: Map<String, Any>) {
        db.collection("users").document(uid).update(updates).await()
    }

    fun getCurrentUserId(): String? = auth.currentUser?.uid
    fun getCurrentUserEmail(): String? = auth.currentUser?.email

    suspend fun getCurrentUserProfileImage(): String? {
        val uid = getCurrentUserId() ?: return null
        return try {
            val document = db.collection("users").document(uid).get().await()
            document.getString("profileImageUrl")
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getCurrentUserFavoriteMovies(): List<String> {
        val uid = getCurrentUserId() ?: return emptyList()
        return try {
            val document = db.collection("users").document(uid).get().await()
            @Suppress("UNCHECKED_CAST")
            document.get("favoriteMovies") as? List<String> ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun addFavoriteMovie(movieId: String) {
        val uid = getCurrentUserId() ?: return
        db.collection("users").document(uid)
            .update("favoriteMovies", FieldValue.arrayUnion(movieId))
            .await()
    }

    suspend fun removeFavoriteMovie(movieId: String) {
        val uid = getCurrentUserId() ?: return
        db.collection("users").document(uid)
            .update("favoriteMovies", FieldValue.arrayRemove(movieId))
            .await()
    }

    suspend fun isMovieFavorite(movieId: String): Boolean {
        val favorites = getCurrentUserFavoriteMovies()
        return favorites.contains(movieId)
    }

    suspend fun getCurrentUserTop3FavoriteGenres(): List<MovieGenre> = coroutineScope {
        val favoriteMovieIds = getCurrentUserFavoriteMovies()
        val movieRepository = MovieRepository()

        val favoriteMovies = favoriteMovieIds.map { id ->
            async { movieRepository.getMovieDetails(id) }
        }.awaitAll().filterNotNull()

        favoriteMovies
            .flatMap { it.genres }
            .groupingBy { it }
            .eachCount()
            .entries
            .sortedByDescending { it.value }
            .take(3)
            .map { it.key }
    }

    fun signOut() {
        auth.signOut()
    }
}
