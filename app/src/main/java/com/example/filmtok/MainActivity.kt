package com.example.filmtok

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.firestore

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        FirebaseApp.initializeApp(this)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
//        val db = Firebase.firestore
//        val testData = hashMapOf("status" to "FilmTok działa!")
//
//        db.collection("polaczenie_testowe")
//            .add(testData)
//            .addOnSuccessListener {
//                Log.d("FirebaseTest", "Sukces! Dane są w chmurze.")
//            }
//            .addOnFailureListener { e ->
//                Log.w("FirebaseTest", "Błąd połączenia", e)
//            }
    }
}