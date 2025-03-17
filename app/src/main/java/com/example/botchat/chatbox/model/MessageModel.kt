package com.example.botchat.chatbox.model

data class MessageModel(
    val message: String,
    val role: String,
    val timestamp: Long= System.currentTimeMillis(),
    val roomId: String? = null,
    val imageUri: String? = null,
    val driveLink: String? = null,
)

