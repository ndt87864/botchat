package com.example.botchat.account.viewModel.forgot


import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ForgotPasswordHandler(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) {
    private var currentEmail: String? = null

    suspend fun checkEmailExists(email: String): Boolean {
        val query = db.collection("users").whereEqualTo("email", email).get().await()
        return !query.isEmpty
    }

    fun sendResetPasswordEmail(email: String) {
        currentEmail = email
        println("Gửi email đặt lại mật khẩu đến $email") // Giả lập gửi email
    }

    suspend fun resetPassword(): Result<String> {
        return try {
            if (currentEmail != null) {
                auth.sendPasswordResetEmail(currentEmail!!).await()
                Result.success("Email đặt lại mật khẩu đã được gửi. Vui lòng kiểm tra hộp thư.")
            } else {
                Result.failure(Exception("Không có email được xác minh"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun clearCurrentEmail() {
        currentEmail = null
    }
}