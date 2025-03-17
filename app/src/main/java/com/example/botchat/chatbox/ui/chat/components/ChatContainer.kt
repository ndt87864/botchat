package com.example.botchat.chatbox.ui.chat.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.botchat.chatbox.ui.chat.widgets.ModelInfo
import com.example.botchat.chatbox.viewModel.ChatViewModel
import com.example.botchat.chatbox.ui.chat.widgets.ScrollToBottomButton
import kotlinx.coroutines.CoroutineScope

@Composable
fun ChatContainer(viewModel: ChatViewModel, listState: androidx.compose.foundation.lazy.LazyListState, coroutineScope: CoroutineScope) {
    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f)) {
            MessageList(
                modifier = Modifier.fillMaxSize(),
                messageList = viewModel.messageList,
                listState = listState,
                viewModel = viewModel
            )
            ScrollToBottomButton(listState, coroutineScope)
        }
        MessageInput(onMessageSend = { message -> viewModel.sendMessage(message) }, viewModel = viewModel)

        ModelInfo(viewModel)
    }
}