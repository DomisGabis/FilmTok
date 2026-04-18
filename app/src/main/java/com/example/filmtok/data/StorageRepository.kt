package com.example.filmtok.data

import android.net.Uri
import com.example.filmtok.model.Movie
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await

class StorageRepository {
    private val storage = FirebaseStorage.getInstance()

    suspend fun uploadImage(uri: Uri, path: String, onProgress: (Float) -> Unit = {}): String {
        return uploadFile(uri, path, onProgress)
    }

    suspend fun uploadVideo(uri: Uri, path: String, onProgress: (Float) -> Unit = {}): String {
        return uploadFile(uri, path, onProgress)
    }

    private suspend fun uploadFile(uri: Uri, path: String, onProgress: (Float) -> Unit): String {
        val ref = storage.reference.child(path)
        val uploadTask = ref.putFile(uri)
        
        uploadTask.addOnProgressListener { snapshot ->
            if (snapshot.totalByteCount > 0) {
                val progress = snapshot.bytesTransferred.toFloat() / snapshot.totalByteCount
                onProgress(progress)
            }
        }.await()
        
        return ref.downloadUrl.await().toString()
    }

    suspend fun getDownloadUrl(pathOrUrl: String): String {
        if (pathOrUrl.isBlank() || pathOrUrl.startsWith("http") || pathOrUrl.startsWith("content://")) {
            return pathOrUrl
        }
        return try {
            storage.reference.child(pathOrUrl).downloadUrl.await().toString()
        } catch (e: Exception) {
            pathOrUrl
        }
    }

    suspend fun resolveMovieUrls(movie: Movie): Movie = coroutineScope {
        val poster = async { getDownloadUrl(movie.posterUrl) }
        val backdrop = async { getDownloadUrl(movie.backdropUrl) }
        val video = async { getDownloadUrl(movie.videoUrl) }
        
        movie.copy(
            posterUrl = poster.await(),
            backdropUrl = backdrop.await(),
            videoUrl = video.await()
        )
    }
}
