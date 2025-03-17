package com.example.botchat.chatbox.ui.history

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.botchat.chatbox.viewModel.ChatViewModel
import com.example.botchat.chatbox.model.MessageModel
import com.example.botchat.chatbox.ui.history.components.ChatHistoryHeader
import com.example.botchat.chatbox.ui.history.components.ChatHistoryList
import com.example.botchat.chatbox.ui.history.components.DeleteConfirmationDialog
import com.example.botchat.chatbox.ui.history.components.EmptyHistoryMessage


@Composable
fun ChatHistoryPage(viewModel: ChatViewModel, modifier: Modifier = Modifier, onChatSelected: (Int) -> Unit) {
    var oldestMessages by remember { mutableStateOf<List<Pair<Int, MessageModel>>>(emptyList()) }
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }

    fun refreshHistory() {
        viewModel.getOldestMessagesPerRoom { messages ->
            oldestMessages = messages.reversed()
        }
    }

    LaunchedEffect(Unit) {
        refreshHistory()
    }

    Box(modifier = modifier) {
        Column {
            ChatHistoryHeader(oldestMessages.isNotEmpty()) { showDeleteDialog = true }
            if (showDeleteDialog) {
                DeleteConfirmationDialog(
                    onConfirm = {
                        viewModel.deleteAllChatRooms(context) { // Xóa trực tiếp collection rooms
                            oldestMessages = emptyList()
                            refreshHistory()
                        }
                        showDeleteDialog = false
                    },
                    onDismiss = { showDeleteDialog = false }
                )
            }
            if (oldestMessages.isEmpty()) {
                EmptyHistoryMessage()
            } else {
                ChatHistoryList(oldestMessages, viewModel, context, onChatSelected, ::refreshHistory)
            }
        }
    }
}









