package com.example.botchat.chatbox.ui.chat

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.botchat.chatbox.ui.chat.components.ChatContainer
import com.example.botchat.chatbox.viewModel.ChatViewModel
import com.example.botchat.chatbox.ui.chat.handlers.HandleUiEvents
import com.example.botchat.chatbox.ui.chat.handlers.startCall


@Composable
fun ChatPage(modifier: Modifier = Modifier, viewModel: ChatViewModel, roomId: Int) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    var lastCallNumber by remember { mutableStateOf<String?>(null) }

    val callPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted && lastCallNumber != null) {
            startCall(context, lastCallNumber!!)
        }
    }

    HandleUiEvents(viewModel, context, callPermissionLauncher, lastCallNumber) { lastCallNumber = it }
    LaunchedEffect(roomId) { viewModel.loadMessagesForRoom(roomId) }

    Column(modifier = modifier.fillMaxSize()) {
        ChatContainer(viewModel, listState, coroutineScope)
    }
}


