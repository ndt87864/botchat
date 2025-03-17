package com.example.botchat.chatbox.viewModel.data

import android.util.Log
import com.example.botchat.chatbox.model.MessageModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirestoreHandler(
    val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    // Lưu tin nhắn vào Firestore
    fun saveMessageToFirestore(message: MessageModel, role: String, userEmail: String, roomId: Int) {
        if (roomId <= 0) {
            Log.e("FirestoreHandler", "Invalid roomId: $roomId. Cannot save message.")
            return
        }

        val roomCollection = firestore.collection("chats")
            .document(userEmail)
            .collection("rooms")
            .document(roomId.toString())

        val messageData = mapOf(
            "message" to message.message,
            "role" to role,
            "timestamp" to message.timestamp,
            "driveLink" to message.driveLink
        )

        roomCollection.collection("messages").add(messageData)
            .addOnSuccessListener {
                Log.d("FirestoreHandler", "Message saved successfully")
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreHandler", "Error saving message: ${e.message}")
            }
    }

    // Tải tin nhắn từ Firestore
    suspend fun loadMessages(roomId: Int, userEmail: String): List<MessageModel> {
        try {
            val messagesSnapshot = firestore.collection("chats")
                .document(userEmail)
                .collection("rooms")
                .document(roomId.toString())
                .collection("messages")
                .orderBy("timestamp")
                .get()
                .await()

            return messagesSnapshot.documents.mapNotNull { doc ->
                val message = doc.getString("message") ?: return@mapNotNull null
                val role = doc.getString("role") ?: "unknown"
                val timestamp = doc.getLong("timestamp") ?: 0L
                val driveLink = doc.getString("driveLink")
                MessageModel(message, role, timestamp, driveLink = driveLink)
            }
        } catch (e: Exception) {
            Log.e("FirestoreHandler", "Error loading messages: ${e.message}")
            return emptyList()
        }
    }
    // Trong FirestoreHandler
    suspend fun deleteMessagesAfterTimestamp(userEmail: String, roomId: Int, timestamp: Long) {
        try {
            val messagesSnapshot = firestore.collection("chats")
                .document(userEmail)
                .collection("rooms")
                .document(roomId.toString())
                .collection("messages")
                .whereGreaterThanOrEqualTo("timestamp", timestamp) // Xóa cả tin nhắn tại timestamp
                .get()
                .await()

            for (doc in messagesSnapshot.documents) {
                doc.reference.delete().await()
            }
            Log.d("FirestoreHandler", "Deleted messages from timestamp $timestamp in room $roomId")
        } catch (e: Exception) {
            Log.e("FirestoreHandler", "Error deleting messages: ${e.message}")
        }
    }
    // Khởi tạo roomId mới
    suspend fun initializeRoomId(userEmail: String): Int {
        try {
            val rooms = firestore.collection("chats")
                .document(userEmail)
                .collection("rooms")
                .get()
                .await()
            val roomNumbers = rooms.documents.mapNotNull { it.id.toIntOrNull() }
            return (roomNumbers.maxOrNull() ?: 0) + 1
        } catch (e: Exception) {
            Log.e("FirestoreHandler", "Error initializing roomId: ${e.message}")
            return 1
        }
    }

    // Tạo phòng chat mới
    fun createNewChatRoom(userEmail: String, roomId: Int) {
        val roomCollection = firestore.collection("chats")
            .document(userEmail)
            .collection("rooms")
            .document(roomId.toString())

        roomCollection.set(mapOf("roomId" to roomId))
            .addOnSuccessListener {
                Log.d("FirestoreHandler", "Room $roomId created successfully for $userEmail")
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreHandler", "Failed to create room: ${e.message}")
            }
    }

    // Lấy tin nhắn cũ nhất từ mỗi phòng
    suspend fun getOldestMessagesPerRoom(userEmail: String): List<Pair<Int, MessageModel>> {
        try {
            val roomsSnapshot = firestore.collection("chats")
                .document(userEmail)
                .collection("rooms")
                .get()
                .await()

            val oldestMessages = mutableListOf<Pair<Int, MessageModel>>()

            for (room in roomsSnapshot.documents) {
                val roomId = room.id.toIntOrNull() ?: continue
                val messagesSnapshot = firestore.collection("chats")
                    .document(userEmail)
                    .collection("rooms")
                    .document(roomId.toString())
                    .collection("messages")
                    .orderBy("timestamp")
                    .limit(1)
                    .get()
                    .await()

                val messageDoc = messagesSnapshot.documents.firstOrNull()
                if (messageDoc != null) {
                    val message = messageDoc.getString("message") ?: ""
                    val role = messageDoc.getString("role") ?: "unknown"
                    val timestamp = messageDoc.getLong("timestamp") ?: 0L
                    oldestMessages.add(roomId to MessageModel(message, role, timestamp))
                }
            }

            oldestMessages.sortBy { it.first }
            return oldestMessages
        } catch (e: Exception) {
            Log.e("FirestoreHandler", "Error fetching oldest messages: ${e.message}")
            return emptyList()
        }
    }

    // Xóa phòng chat
    suspend fun deleteRoom(userEmail: String, roomId: Int): Boolean {
        try {
            val roomDocRef = firestore.collection("chats")
                .document(userEmail)
                .collection("rooms")
                .document(roomId.toString())

            roomDocRef.delete().await()
            val snapshotAfterDelete = roomDocRef.get().await()
            return !snapshotAfterDelete.exists()
        } catch (e: Exception) {
            Log.e("FirestoreHandler", "Error deleting room: ${e.message}")
            return false
        }
    }

    // Kiểm tra xem user có rooms không
    suspend fun hasRooms(userEmail: String): Boolean {
        return try {
            val roomsSnapshot = firestore.collection("chats")
                .document(userEmail)
                .collection("rooms")
                .get()
                .await()
            roomsSnapshot.documents.isNotEmpty()
        } catch (e: Exception) {
            Log.e("FirestoreHandler", "Error checking rooms: ${e.message}")
            false
        }
    }

    // Tải cài đặt người dùng từ Firestore
    fun loadUserSettings(
        userEmail: String,
        onSettingsLoaded: (applyCustomization: Boolean, userName: String, occupation: String, botTraits: String, additionalInfo: String) -> Unit
    ) {
        firestore.collection("user_settings")
            .document(userEmail)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("FirestoreHandler", "Error loading user settings: ${e.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val applyCustomization = snapshot.getBoolean("applyCustomization") ?: false
                    val userName = snapshot.getString("userName") ?: "Bạn"
                    val occupation = snapshot.getString("occupation") ?: ""
                    val botTraits = snapshot.getString("botTraits") ?: ""
                    val additionalInfo = snapshot.getString("additionalInfo") ?: ""
                    onSettingsLoaded(applyCustomization, userName, occupation, botTraits, additionalInfo)
                }
            }
    }

    // Cập nhật cài đặt người dùng vào Firestore
    suspend fun updateSettings(
        userEmail: String,
        applyCustomization: Boolean,
        userName: String,
        occupation: String,
        botTraits: String,
        additionalInfo: String
    ): Result<Unit> {
        return try {
            val settings = hashMapOf(
                "applyCustomization" to applyCustomization,
                "userName" to userName,
                "occupation" to occupation,
                "botTraits" to botTraits,
                "additionalInfo" to additionalInfo
            )

            firestore.collection("user_settings")
                .document(userEmail)
                .set(settings)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirestoreHandler", "Error updating settings: ${e.message}")
            Result.failure(e)
        }
    }
}