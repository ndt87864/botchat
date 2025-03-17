package com.example.botchat.chatbox.ui.history.components

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.botchat.chatbox.model.MessageModel
import com.example.botchat.chatbox.viewModel.ChatViewModel


@Composable
fun ChatHistoryItem(
    chat: Pair<Int, MessageModel>,
    isNewest: Boolean,
    viewModel: ChatViewModel,
    context: Context,
    onChatSelected: (Int) -> Unit,
    refreshHistory: () -> Unit
) {
    val roomId = chat.first
    val message = chat.second
    val displayMessage = if (message.message.length > 50) message.message.take(10) + "..." else message.message
    var showMenu by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isNewest) Color(0xFFBBDEFB) else Color(0xFFF1F1F1))
            .border(width = if (isNewest) 2.dp else 1.dp, color = if (isNewest) Color(0xFF1976D2) else Color.Gray, shape = RoundedCornerShape(12.dp))
            .padding(16.dp)
            .clickable { onChatSelected(roomId) }
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Column {
                Text(text = "Room ID: $roomId", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = displayMessage, style = MaterialTheme.typography.bodyLarge, color = if (isNewest) Color.Black else Color.DarkGray)
            }
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = { showMenu = true }) {
                Icon(imageVector = Icons.Default.MoreVert, contentDescription = "More Options")
            }
        }
        if (showMenu) {
            ChatHistoryMenu(viewModel, context, roomId, refreshHistory) { showMenu = false }
        }
    }
}
