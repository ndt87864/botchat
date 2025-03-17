package com.example.botchat.chatbox.ui.chat.widgets

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.botchat.chatbox.viewModel.ChatViewModel

@Composable
fun ModelInfo(viewModel: ChatViewModel) {
    val currentDisplayName = viewModel.modelNameMapping[viewModel.currentModelName] ?: viewModel.currentModelName
    Text(
        text = "Model: $currentDisplayName",
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        textAlign = TextAlign.Center
    )
}