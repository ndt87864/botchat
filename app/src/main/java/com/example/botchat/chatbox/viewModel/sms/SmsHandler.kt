package com.example.botchat.chatbox.viewModel.sms

import android.content.Context
import android.telephony.SmsManager
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import com.example.botchat.chatbox.viewModel.ChatViewModel
import com.example.botchat.chatbox.model.UiEvent
import com.example.botchat.chatbox.model.MessageModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.Channel

class SMSHandler(
    private val _event: Channel<UiEvent>,
    private val messageList: MutableList<MessageModel>,
    private val saveMessage: (MessageModel) -> Unit
) {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    var isMessageMode = mutableStateOf(false)

    init {
        val userEmail = auth.currentUser?.email
        userEmail?.let {
            loadMessageModeState(it) { isMessageMode.value = it }
        }
    }

    fun toggleMessageMode(context: Context) {

        isMessageMode.value = !isMessageMode.value
        val message = if (isMessageMode.value) {
            "Chế độ gửi tin nhắn đã bật."
        } else {
            "Chế độ gửi tin nhắn đã tắt."
        }
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        val userEmail = auth.currentUser?.email ?: return
        saveMessageModeState(userEmail, isMessageMode.value)
    }


    private fun saveMessageModeState(userEmail: String, isMessageMode: Boolean) {
        val userSettings = firestore.collection("chats")
            .document(userEmail)
            .collection("settings")
            .document("user_settings")

        userSettings.set(mapOf("isMessageMode" to isMessageMode), SetOptions.merge())
            .addOnSuccessListener {
                Log.d("MessageHandler", "Message mode state saved successfully")
            }
            .addOnFailureListener { e ->
                Log.e("MessageHandler", "Error saving Message mode state: ${e.message}")
            }
    }

    private fun loadMessageModeState(userEmail: String, onResult: (Boolean) -> Unit) {
        val userSettings = firestore.collection("chats")
            .document(userEmail)
            .collection("settings")
            .document("user_settings")

        userSettings.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val isMessageMode = document.getBoolean("isMessageMode") ?: false
                    onResult(isMessageMode)
                } else {
                    onResult(false)
                }
            }
            .addOnFailureListener { e ->
                Log.e("MessageHandler", "Error loading Message mode state: ${e.message}")
                onResult(false)
            }
    }

    // Hàm kiểm tra lệnh gửi tin nhắn
    fun isMessageCommand(message: String): Boolean {
        return message.startsWith("gửi tin nhắn", ignoreCase = true) ||
                message.startsWith("nhắn", ignoreCase = true)
    }

    fun extractMessageDetails(message: String): Pair<String?, String?> {
        val pattern1 = Regex("(?i)gửi\\s+tin\\s+nhắn\\s+(.+?)\\s+(?:đến|to)\\s+([0-9#+]+)")
        val pattern2 = Regex("(?i)gửi\\s+tin\\s+nhắn\\s+(?:đến|to)\\s+([0-9#+]+).*?(?:với\\s+nội\\s+dung|nội\\s+dung)\\s+(.+)")

        pattern1.find(message)?.let { match ->
            val content = match.groupValues[1].trim()
            val phone = match.groupValues[2].trim()
            return phone to content
        }

        pattern2.find(message)?.let { match ->
            val phone = match.groupValues[1].trim()
            val content = match.groupValues[2].trim()
            return phone to content
        }

        return null to null
    }

    // Hàm xử lý gửi tin nhắn
    fun handleMessageCommand(question: String) {
        if (isMessageCommand(question)) {
            val (number, content) = extractMessageDetails(question)
            if (number != null && content != null) {

                messageList.removeLast() // Xóa "Đang gõ..."
                // Tạo một tin nhắn đặc biệt để hiển thị box thông báo
                val messageBoxContent = """
                    **Yêu cầu gửi tin nhắn**  
                    - **Số điện thoại**: $number  
                    - **Nội dung**: $content  
                    [Sửa đổi](#edit) | [Gửi](#send|$number|$content)
                """.trimIndent()
                val messageBox = MessageModel(messageBoxContent, "model", System.currentTimeMillis())
                messageList.add(messageBox)
                if(!ChatViewModel().isAnonymous){
                    saveMessage(messageBox)
                }
            } else {
                val errorMessage = MessageModel(
                    "Không thể gửi tin nhắn. Vui lòng cung cấp số điện thoại hoặc nội dung hợp lệ.VD : gửi tin nhắn đến 191 nội dung KTTK",
                    "model",
                    System.currentTimeMillis()
                )
                messageList.add(errorMessage)

                if(!ChatViewModel().isAnonymous){
                    saveMessage(errorMessage)
                }
            }
        }
    }

    // Hàm thực hiện gửi tin nhắn khi người dùng nhấn "Gửi"
    fun sendMessage(number: String, content: String) {

        try {
            val smsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(number, null, content, null, null)
            val successMessage = MessageModel(
                "Đã gửi tin nhắn '$content' đến $number",
                "model",
                System.currentTimeMillis()
            )
            messageList.add(successMessage)

            if(!ChatViewModel().isAnonymous){
                saveMessage(successMessage)
            }
        } catch (e: Exception) {
            val errorMessage = MessageModel(
                "Lỗi khi gửi tin nhắn: ${e.message}",
                "model",
                System.currentTimeMillis()
            )
            messageList.add(errorMessage)

            if(!ChatViewModel().isAnonymous){
                saveMessage(errorMessage)
            }
        }
    }
}