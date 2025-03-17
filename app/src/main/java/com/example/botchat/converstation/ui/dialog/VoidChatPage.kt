package com.example.botchat.converstation.ui.dialog

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.botchat.chatbox.model.MessageModel
import com.example.botchat.chatbox.viewModel.ChatViewModel
import com.example.botchat.converstation.ui.dialog.components.ExitVoidChat
import com.example.botchat.converstation.viewModel.VoidChatViewModel
import com.example.botchat.converstation.ui.dialog.widget.AnimatedCircle
import com.example.botchat.converstation.ui.dialog.widget.RecordingControl
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun VoidChatPage(
    modifier: Modifier = Modifier,
    viewModel: VoidChatViewModel = viewModel(),
    chatViewModel: ChatViewModel = viewModel(),
    onExitSummaryShown: (Boolean) -> Unit = {}, // Callback trạng thái tóm tắt
    onExitRequested: () -> Unit = {} // Callback yêu cầu thoát
) {
    val context = LocalContext.current
    var isActive by remember { mutableStateOf(true) }
    var showExitSummary by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.initialize(context)
        if (viewModel.conversation.isEmpty()) {
            viewModel.greetUser(context)
            viewModel.conversation.clear()
        }
    }

    // Báo trạng thái showExitSummary cho TopBar
    LaunchedEffect(showExitSummary) {
        onExitSummaryShown(showExitSummary)
    }

    // Xử lý BackHandler
    BackHandler(enabled = true) {
        if (showExitSummary) {
            // Khi ở trang tóm tắt, thực hiện thoát thông qua callback
            viewModel.reset()
            viewModel.conversation.clear()
            onExitRequested() // Gọi callback thay vì onBackPressed
        } else {
            if (viewModel.isBotSpeaking.value) {
                viewModel.stopBotSpeaking()
            }
            showExitSummary = true // Hiển thị trang tóm tắt
        }
    }

    if (showExitSummary) {
        ExitVoidChat(
            conversation = viewModel.conversation,
            onExit = {
                viewModel.reset()
                viewModel.conversation.clear()
                onExitRequested() // Gọi callback thay vì onBackPressed
            },
            onSave = {
                val userEmail = FirebaseAuth.getInstance().currentUser?.email ?: "default@example.com"
                scope.launch {
                    if (chatViewModel.messageList.isEmpty()) {
                        viewModel.conversation.forEach { (userMsg, botMsg) ->
                            if (userMsg.isNotBlank()) {
                                chatViewModel.chatRoomManager.saveMessage(
                                    message = MessageModel(
                                        message = userMsg,
                                        role = "userVoice",
                                        timestamp = System.currentTimeMillis()
                                    ),
                                    role = "userVoice",
                                    userEmail = userEmail
                                )
                            }
                            if (botMsg.isNotBlank()) {
                                chatViewModel.chatRoomManager.saveMessage(
                                    message = MessageModel(
                                        message = botMsg,
                                        role = "modelVoice",
                                        timestamp = System.currentTimeMillis()
                                    ),
                                    role = "modelVoice",
                                    userEmail = userEmail
                                )
                            }
                            chatViewModel.createNewChatRoom()
                        }
                    } else {
                        chatViewModel.createNewChatRoom()
                        viewModel.conversation.forEach { (userMsg, botMsg) ->
                            if (userMsg.isNotBlank()) {
                                chatViewModel.chatRoomManager.saveMessage(
                                    message = MessageModel(
                                        message = userMsg,
                                        role = "userVoice",
                                        timestamp = System.currentTimeMillis()
                                    ),
                                    role = "userVoice",
                                    userEmail = userEmail
                                )
                            }
                            if (botMsg.isNotBlank()) {
                                chatViewModel.chatRoomManager.saveMessage(
                                    message = MessageModel(
                                        message = botMsg,
                                        role = "modelVoice",
                                        timestamp = System.currentTimeMillis()
                                    ),
                                    role = "modelVoice",
                                    userEmail = userEmail
                                )
                            }
                        }
                    }
                    viewModel.reset()
                    viewModel.conversation.clear()
                    onExitRequested()
                }
            }
        )
    } else {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Đàm thoại bằng giọng nói",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                if (isActive) {
                    AnimatedCircle(
                        isRecording = viewModel.isRecording.value,
                        isBotSpeaking = viewModel.isBotSpeaking.value,
                        sizeDp = 220
                    )
                }
            }

            RecordingControl(
                viewModel = viewModel,
                onExitClick = { showExitSummary = true }
            )
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            isActive = false
            if (viewModel.isRecording.value) {
                viewModel.stopRecording()
            }
            if (viewModel.isBotSpeaking.value) {
                viewModel.stopBotSpeaking()
            }
        }
    }
}