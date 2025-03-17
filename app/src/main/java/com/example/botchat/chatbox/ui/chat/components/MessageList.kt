package com.example.botchat.chatbox.ui.chat.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.botchat.R
import com.example.botchat.chatbox.viewModel.ChatViewModel
import com.example.botchat.chatbox.model.MessageModel
import com.example.botchat.ui.theme.Purple80

@Composable
fun MessageList(
    modifier: Modifier = Modifier,
    messageList: List<MessageModel>,
    listState: LazyListState,
    viewModel: ChatViewModel // Thêm tham số viewModel
) {
    if (messageList.isEmpty()) {
        Column(
            modifier = modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                modifier = Modifier.size(100.dp),
                painter = painterResource(id = R.drawable.de2),
                contentDescription = "Icon",
            )
            Text(text = "Tôi có thể giúp gì cho bạn ?", fontSize = 22.sp)
        }
    } else {
        LazyColumn(
            modifier = modifier,
            state = listState,
            reverseLayout = true
        ) {
            items(messageList.reversed()) {
                MessageRow(messageModel = it, viewModel = viewModel) // Truyền viewModel vào MessageRow
            }
        }
    }
}