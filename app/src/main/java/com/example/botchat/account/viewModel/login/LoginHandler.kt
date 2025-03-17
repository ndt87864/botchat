package com.example.botchat.account.viewModel

import com.example.botchat.chatbox.viewModel.ChatViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class LoginHandler(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) {
    suspend fun loginUser(email: String, password: String): Boolean {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            if (result.user != null) {
                ChatViewModel().createNewChatRoom()
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    suspend fun signInWithGoogle(googleIdToken: String, displayName: String?, email: String?, dateOfBirth: String? = null): Boolean {
        return try {
            val credential = GoogleAuthProvider.getCredential(googleIdToken, null)
            val result = auth.signInWithCredential(credential).await()
            val user = result.user
            if (user != null) {
                val document = db.collection("users").document(user.uid).get().await()
                if (!document.exists()) {
                    val userData = mapOf(
                        "username" to (displayName ?: "Unknown"),
                        "email" to (email ?: ""),
                        "dateOfBirth" to (dateOfBirth ?: "")
                    )
                    db.collection("users").document(user.uid).set(userData).await()
                }
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
}