package com.example.filmtok.data

import com.example.filmtok.model.CastMember
import com.example.filmtok.model.Movie
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class MovieRepository {
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun getHeroMovie(): Movie {
        // Logika pobierania konkretnego filmu dnia
        return try {
            firestore.collection("movies")
                .document("hero_movie_id") // Przykładowe ID
                .get()
                .await()
                .toObject(Movie::class.java) ?: getMockHero()
        } catch (e: Exception) {
            getMockHero()
        }
    }

    suspend fun getRecentlyWatched(): List<Movie> {
        return try {
            firestore.collection("movies")
                .limit(10)
                .get()
                .await()
                .toObjects(Movie::class.java)
        } catch (e: Exception) {
            getMockRecentlyWatched()
        }
    }

    suspend fun getMovieDetails(movieId: String): Movie {
        return try {
            firestore.collection("movies")
                .document(movieId)
                .get()
                .await()
                .toObject(Movie::class.java) ?: getMockDetails(movieId)
        } catch (e: Exception) {
            getMockDetails(movieId)
        }
    }

    suspend fun getReels(): List<Movie> {
        return try {
            // W realnej aplikacji pobieralibyśmy z innej kolekcji lub z flagą isReel
            firestore.collection("movies")
                .whereEqualTo("hasVideo", true)
                .get()
                .await()
                .toObjects(Movie::class.java).ifEmpty { getMockReels() }
        } catch (e: Exception) {
            getMockReels()
        }
    }

    private fun getMockReels() = listOf(
        Movie(
            id = "1",
            title = "Dune: Part Two - Zwiastun",
            description = "Niesamowite ujęcia z najnowszej części Diuny! 🏜️",
            videoUrl = "https://example.com/video1.mp4",
            posterUrl = "https://image.tmdb.org/t/p/w1280/xOMo8NETsO2HnaUfs0vqdI2ofD7.jpg",
            likesCount = 1240,
            commentsCount = 84
        ),
        Movie(
            id = "2",
            title = "Blade Runner 2049 - Klimat",
            description = "Neonowa przyszłość w obiektywie Rogera Deakinsa.",
            videoUrl = "https://example.com/video2.mp4",
            posterUrl = "https://image.tmdb.org/t/p/w1280/gajva2L0rQ6vO9YvS9mS6SAt5Z8.jpg",
            likesCount = 850,
            commentsCount = 42
        )
    )

    suspend fun getAllMovies(): List<Movie> {
        return try {
            firestore.collection("movies")
                .get()
                .await()
                .toObjects(Movie::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun deleteMovie(movieId: String) {
        firestore.collection("movies").document(movieId).delete().await()
    }

    suspend fun saveMovie(movie: Movie) {
        val docRef = if (movie.id.isEmpty()) {
            firestore.collection("movies").document()
        } else {
            firestore.collection("movies").document(movie.id)
        }
        val movieToSave = if (movie.id.isEmpty()) movie.copy(id = docRef.id) else movie
        docRef.set(movieToSave).await()
    }

    private fun getMockHero() = Movie(
        id = "1",
        title = "Dune: Part Two",
        posterUrl = "https://image.tmdb.org/t/p/w1280/8uVKfR6VoBovuSpecial.jpg",
        backdropUrl = "https://image.tmdb.org/t/p/w1280/8uVKfR6VoBovuSpecial.jpg",
        genres = listOf("Sci-Fi", "Action")
    )

    private fun getMockDetails(id: String) = Movie(
        id = id,
        title = "Dune: Part Two",
        posterUrl = "https://image.tmdb.org/t/p/w500/8uVKfR6VoBovuSpecial.jpg",
        backdropUrl = "https://image.tmdb.org/t/p/w1280/xOMo8NETsO2HnaUfs0vqdI2ofD7.jpg",
        genres = listOf("Sci-Fi", "Action"),
        year = 2024,
        rating = 8.8,
        duration = "166 min",
        director = "Denis Villeneuve",
        description = "Paul Atreides unites with Chani and the Fremen while on a warpath of revenge against the conspirators who destroyed his family. Facing a choice between the love of his life and the fate of the known universe, he endeavors to prevent a terrible future only he can foresee.",
        cast = listOf(
            CastMember(
                "1",
                "Timothée Chalamet",
                "Paul Atreides",
                "https://image.tmdb.org/t/p/w200/BE79796YInS92v9pG0EIpC9900.jpg"
            ),
            CastMember(
                "2",
                "Zendaya",
                "Chani",
                "https://image.tmdb.org/t/p/w200/j9S90P8v696894080175.jpg"
            ),
            CastMember(
                "3",
                "Rebecca Ferguson",
                "Lady Jessica",
                "https://image.tmdb.org/t/p/w200/lJ6LCOcl097893116345.jpg"
            )
        ),
        gallery = listOf(
            "https://image.tmdb.org/t/p/w1280/xOMo8NETsO2HnaUfs0vqdI2ofD7.jpg",
            "https://image.tmdb.org/t/p/w1280/676f62590212450849942767.jpg",
            "https://image.tmdb.org/t/p/w1280/676f62645831630843633633.jpg"
        )
    )

    private fun getMockRecentlyWatched() = listOf(
        Movie(
            "1",
            "Dune: Part Two",
            "https://image.tmdb.org/t/p/w500/8uVKfR6VoBovuSpecial.jpg",
            year = 2024
        ),
        Movie(
            "2",
            "Blade Runner 2049",
            "https://image.tmdb.org/t/p/w500/gajva2L0rQ6vO9YvS9mS6SAt5Z8.jpg",
            year = 2017
        ),
        Movie(
            "3",
            "Interstellar",
            "https://image.tmdb.org/t/p/w500/gEU2QniE6EwfVnzCuf24hxIn2nn.jpg",
            year = 2014
        )
    )
}