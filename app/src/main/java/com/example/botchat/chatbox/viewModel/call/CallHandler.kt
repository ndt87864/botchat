// CallHandler.kt
package com.example.botchat.chatbox.viewModel.call

import android.content.Context
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import com.example.botchat.chatbox.viewModel.ChatViewModel
import com.example.botchat.chatbox.model.MessageModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class CallHandler(
    private val _event: Channel<UiEvent>,
    private val messageList: MutableList<MessageModel>,
    private val saveMessage: (MessageModel) -> Unit
) {

    val firestore = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    var isCallMode = mutableStateOf(false)

    init {
        val userEmail = auth.currentUser?.email
        userEmail?.let {
            loadCallModeState(it) { isCallMode.value = it }
        }
    }

    fun toggleCallMode(context: Context) {
        isCallMode.value = !isCallMode.value
        val message = if (isCallMode.value) {
            "Chế độ gọi điện đã bật."
        } else {
            "Chế độ gọi điện đã tắt."
        }
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        val userEmail = auth.currentUser?.email ?: return
        saveCallModeState(userEmail, isCallMode.value)
    }

    private fun saveCallModeState(userEmail: String, isCallMode: Boolean) {
        val userSettings = firestore.collection("chats")
            .document(userEmail)
            .collection("settings")
            .document("user_settings")

        userSettings.set(mapOf("isCallMode" to isCallMode), SetOptions.merge())
            .addOnSuccessListener {
                Log.d("CallHandler", "Call mode state saved successfully")
            }
            .addOnFailureListener { e ->
                Log.e("CallHandler", "Error saving Call mode state: ${e.message}")
            }
    }

    private fun loadCallModeState(userEmail: String, onResult: (Boolean) -> Unit) {
        val userSettings = firestore.collection("chats")
            .document(userEmail)
            .collection("settings")
            .document("user_settings")

        userSettings.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val isCallMode = document.getBoolean("isCallMode") ?: false
                    onResult(isCallMode)
                } else {
                    onResult(false)
                }
            }
            .addOnFailureListener { e ->
                Log.e("CallHandler", "Error loading Call mode state: ${e.message}")
                onResult(false)
            }
    }
    // Hàm kiểm tra lệnh gọi điện
    fun isCallCommand(message: String): Boolean {
        return message.startsWith("gọi điện", ignoreCase = true) ||
                message.startsWith("gọi", ignoreCase = true)
    }

    // Hàm kiểm tra lệnh quay số
    fun isDialCommand(message: String): Boolean {
        return message.startsWith("nhập số", ignoreCase = true) ||
                message.startsWith("quay số", ignoreCase = true)
    }

    // Hàm tách số điện thoại
    fun extractPhoneNumber(message: String): String? {
        val phoneNumber = message.replace("[^0-9#*]+".toRegex(), "")
        return phoneNumber.takeIf { it.isNotEmpty() }?.let {
            it.replace("#", Uri.encode("#")) // Giữ dấu #
        }
    }

    // Hàm xử lý gọi điện
    suspend fun handleCallCommand(question: String) {
        if (isCallCommand(question)) {
            val number = extractPhoneNumber(question)
            if (number != null) {
                val callingMessage = MessageModel("Đang gọi số $number...", "model", System.currentTimeMillis())
                messageList.add(callingMessage)
                if(!ChatViewModel().isAnonymous){
                    saveMessage(callingMessage)
                }
                delay(2000)
                _event.send(UiEvent.MakePhoneCall(number))
            } else {
                val errorMessage = MessageModel("Số điện thoại không hợp lệ", "model", System.currentTimeMillis())
                messageList.add(errorMessage)
                if(!ChatViewModel().isAnonymous){
                    saveMessage(errorMessage)
                }
            }
        }
    }

    // Hàm xử lý quay số
    suspend fun handleDialCommand(question: String) {
        if (isDialCommand(question)) {
            val number = extractPhoneNumber(question)
            if (number != null) {
                val dialMessage = MessageModel("Mở ứng dụng quay số $number...", "model", System.currentTimeMillis())
                messageList.add(dialMessage)
                if(!ChatViewModel().isAnonymous){
                    saveMessage(dialMessage)
                }
                delay(3000)
                _event.send(UiEvent.OpenDialer(number))
            } else {
                val errorMessage = MessageModel("Số điện thoại không hợp lệ", "model", System.currentTimeMillis())
                messageList.add(errorMessage)

                if(!ChatViewModel().isAnonymous){
                    saveMessage(errorMessage)
                }
            }
        }
    }
}
