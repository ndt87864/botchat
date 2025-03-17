package com.example.botchat.chatbox.ui.history.components

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import com.example.botchat.chatbox.viewModel.ChatViewModel

@Composable
fun ChatHistoryMenu(viewModel: ChatViewModel, context: Context, roomId: Int, refreshHistory: () -> Unit, onDismiss: () -> Unit) {
    Popup(alignment = Alignment.TopEnd, onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .background(Color.White, RoundedCornerShape(8.dp))
                .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                TextButton(onClick = {
                    viewModel.deleteMessage(context, roomId) {
                        refreshHistory()
                    }
                    onDismiss()
                }) {
                    Text("Xóa", color = Color.Black)
                }
            }
        }
    }
}