package com.example.filmtok.data

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
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
}
