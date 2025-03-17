package com.example.botchat.chatbox.ui.history.components

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.botchat.chatbox.model.MessageModel
import com.example.botchat.chatbox.viewModel.ChatViewModel

@Composable
fun ChatHistoryList(
    messages: List<Pair<Int, MessageModel>>,
    viewModel: ChatViewModel,
    context: Context,
    onChatSelected: (Int) -> Unit,
    refreshHistory: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(messages) { index, pair ->
            ChatHistoryItem(pair, index == 0, viewModel, context, onChatSelected, refreshHistory)
        }
    }
}