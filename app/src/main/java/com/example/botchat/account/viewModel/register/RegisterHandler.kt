package com.example.botchat.account.viewModel.register

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class RegisterHandler(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) {
    suspend fun registerUser(email: String, password: String, username: String, dateOfBirth: String) {
        try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user
            user?.let {
                val userData = mapOf(
                    "username" to username,
                    "email" to email,
                    "dateOfBirth" to dateOfBirth
                )
                db.collection("users").document(it.uid).set(userData).await()
            }
        } catch (e: Exception) {
            throw e
        }
    }
}