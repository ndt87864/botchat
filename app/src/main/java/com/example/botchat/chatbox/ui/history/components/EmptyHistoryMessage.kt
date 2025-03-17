package com.example.botchat.chatbox.ui.history.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun EmptyHistoryMessage() {
    Text(
        text = "Không có lịch sử trò chuyện.",
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.fillMaxWidth().wrapContentWidth(align = Alignment.CenterHorizontally)
    )
}