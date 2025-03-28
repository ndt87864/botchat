package com.example.botchat.chatbox.viewModel.data

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.mutableStateListOf
import com.example.botchat.chatbox.model.MessageModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ChatRoomManager(
    private val firestoreHandler: FirestoreHandler,
    private val auth: FirebaseAuth,
    private val messageList: MutableList<MessageModel>,
    private val onRoomIdChanged: (Int) -> Unit
) {

    var roomId: Int = 0
        set(value) {
            field = value
            onRoomIdChanged(value) // Gọi callback khi roomId thay đổi
        }

    // Khởi tạo roomId khi bắt đầu
    suspend fun initialize(userEmail: String): Int {
        roomId = firestoreHandler.initializeRoomId(userEmail)
        loadMessages(userEmail)
        return roomId
    }

    // Tải tin nhắn cho phòng hiện tại
    suspend fun loadMessages(userEmail: String) {
        val messages = firestoreHandler.loadMessages(roomId, userEmail)
        messageList.clear()
        messageList.addAll(messages)
    }

    // Tải tin nhắn cho một phòng cụ thể
    suspend fun loadMessagesForRoom(roomId: Int, userEmail: String) {
        this.roomId = roomId
        val messages = firestoreHandler.loadMessages(roomId, userEmail)
        messageList.clear()
        messageList.addAll(messages)
    }

    // Lấy ID của phòng mới nhất
    suspend fun getLatestChatRoomId(userEmail: String): Int? {
        return firestoreHandler.getLatestChatRoomId(userEmail)
    }

    // Tạo phòng chat mới nếu phòng hiện tại có nội dung
    suspend fun createNewChatRoom(userEmail: String) {
        val latestRoomId = getLatestChatRoomId(userEmail)

        if (roomId != latestRoomId) {
            if (latestRoomId != null && firestoreHandler.isRoomEmpty(latestRoomId, userEmail)) {
                roomId = latestRoomId
                loadMessages(userEmail)
            } else {
                // Tạo phòng mới
                val newRoomId = firestoreHandler.initializeRoomId(userEmail)
                roomId = newRoomId
                messageList.clear()
                firestoreHandler.createNewChatRoom(userEmail, newRoomId)
                loadMessages(userEmail)
            }
        } else {
            if (!messageList.isEmpty()) {
                val newRoomId = firestoreHandler.initializeRoomId(userEmail)
                roomId = newRoomId
                messageList.clear()
                firestoreHandler.createNewChatRoom(userEmail, newRoomId)
                loadMessages(userEmail)
            }
        }
    }

    // Lấy tin nhắn cũ nhất từ mỗi phòng
    suspend fun getOldestMessagesPerRoom(userEmail: String): List<Pair<Int, MessageModel>> {
        return firestoreHandler.getOldestMessagesPerRoom(userEmail)
    }

    // Xóa phòng chat
    suspend fun deleteRoom(context: Context, roomId: Int, userEmail: String, onSuccess: () -> Unit) {
        val success = firestoreHandler.deleteRoom(userEmail, roomId)
        if (success) {
            onSuccess()
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Phòng chat $roomId và tin nhắn đã được xóa", Toast.LENGTH_SHORT).show()
            }
        } else {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Lỗi khi xóa phòng chat", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Lưu tin nhắn vào Firestore
    fun saveMessage(message: MessageModel, role: String, userEmail: String) {
        firestoreHandler.saveMessageToFirestore(message, role, userEmail, roomId)
    }
}