package com.example.filmtok.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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

    fun signOut() {
        auth.signOut()
    }
}