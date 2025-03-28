package com.example.botchat.account.viewModel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.botchat.BuildConfig
import com.example.botchat.R
import com.example.botchat.account.viewModel.edit.EditAccountHandler
import com.example.botchat.account.viewModel.forgot.ForgotPasswordHandler
import com.example.botchat.account.viewModel.register.RegisterHandler
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.ListenerRegistration

class AccountViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val loginHandler = LoginHandler(auth, db) // Cập nhật để truyền db
    private val registerHandler = RegisterHandler(auth, db)
    private val editAccountHandler = EditAccountHandler(auth, db)
    private val forgotPasswordHandler = ForgotPasswordHandler(auth, db)

    private var userListener: ListenerRegistration? = null

    var userName by mutableStateOf<String?>(null)
        private set
    var userEmail by mutableStateOf<String?>(null)
        private set
    var userDateOfBirth by mutableStateOf<String?>(null)
        private set
    var isLoggedIn by mutableStateOf(false)

    fun startUserListener() {
        val user = auth.currentUser
        if (user != null) {
            userListener = db.collection("users")
                .document(user.uid)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener
                    if (snapshot != null && snapshot.exists()) {
                        userName = snapshot.getString("username")
                        userEmail = snapshot.getString("email")
                        userDateOfBirth = snapshot.getString("dateOfBirth")
                    }
                }
        }
    }

    fun stopUserListener() {
        userListener?.remove()
        userListener = null
    }

    fun logoutUser(context: Context) {
        auth.signOut()
        val googleClientId =  BuildConfig.google_client_id
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(googleClientId)
            .requestEmail()
            .build()
        val googleSignInClient = GoogleSignIn.getClient(context, gso)
        googleSignInClient.signOut().addOnCompleteListener {}

        stopUserListener()

        userName = null
        userEmail = null
        userDateOfBirth = null
        isLoggedIn = false
    }

    suspend fun signInWithGoogle(googleIdToken: String, displayName: String?, email: String?, dateOfBirth: String? = null): Boolean {
        val success = loginHandler.signInWithGoogle(googleIdToken, displayName, email, dateOfBirth)
        println("Sign-in success: $success, isLoggedIn before: $isLoggedIn")
        if (success) isLoggedIn = true
        println("isLoggedIn after: $isLoggedIn")
        return success
    }

    suspend fun loginUser(email: String, password: String): Boolean {
        val success = loginHandler.loginUser(email, password)
        if (success) isLoggedIn = true
        return success
    }

    suspend fun registerUser(email: String, password: String, username: String, dateOfBirth: String) {
        registerHandler.registerUser(email, password, username, dateOfBirth)
    }

    suspend fun updateUserInfo(username: String, email: String,currentPassword:String, password: String, dateOfBirth: String): Result<String> {
        return editAccountHandler.updateUserInfo(username, email,currentPassword, password, dateOfBirth)
    }

    suspend fun checkEmailExists(email: String): Boolean {
        return forgotPasswordHandler.checkEmailExists(email)
    }

    fun sendResetPasswordEmail(email: String) {
        forgotPasswordHandler.sendResetPasswordEmail(email)
    }

    suspend fun resetPassword(): Result<String> {
        return forgotPasswordHandler.resetPassword()
    }

    fun clearCurrentEmail() {
        forgotPasswordHandler.clearCurrentEmail()
    }

    fun isUserLoggedIn(): Boolean {
        isLoggedIn = auth.currentUser != null
        return isLoggedIn
    }

    suspend fun getUserName(): String? {
        val user = auth.currentUser
        return user?.let {
            try {
                val document = db.collection("users").document(it.uid).get().await()
                document.getString("username")
            } catch (e: Exception) {
                null
            }
        }
    }

    suspend fun getUserEmail(): String? {
        val user = auth.currentUser
        return user?.let {
            try {
                val document = db.collection("users").document(it.uid).get().await()
                document.getString("email")
            } catch (e: Exception) {
                null
            }
        }
    }

    suspend fun getUserDateOfBirth(): String? {
        val user = auth.currentUser
        return user?.let {
            try {
                val document = db.collection("users").document(it.uid).get().await()
                document.getString("dateOfBirth")
            } catch (e: Exception) {
                null
            }
        }
    }
}