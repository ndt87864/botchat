package com.example.botchat.chatbox.ui.setup

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.botchat.chatbox.ui.setup.components.AdditionalInfoSection
import com.example.botchat.chatbox.ui.setup.components.BotTraitsButtons
import com.example.botchat.chatbox.ui.setup.components.BotTraitsSection
import com.example.botchat.chatbox.viewModel.ChatViewModel


@Composable
fun SettingChatBot(
    modifier: Modifier = Modifier,
    chatViewModel: ChatViewModel,
    onSaveSuccess: () -> Unit
) {
    var showBotTraitsSheet by remember { mutableStateOf(false) }
    var showAdditionalInfoSheet by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .padding(16.dp)
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Cài đặt cá nhân hóa",
            style = MaterialTheme.typography.headlineSmall
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Cho phép áp dụng với các đoạn chat mới")
            Switch(
                checked = chatViewModel.applyCustomization,
                onCheckedChange = { chatViewModel.applyCustomization = it }
            )
        }

        Text("ChatBot sẽ gọi bạn là gì?")
        OutlinedTextField(
            value = chatViewModel.userName,
            onValueChange = { chatViewModel.userName = it },
            modifier = Modifier.fillMaxWidth()
        )

        Text("Bạn làm công việc gì?")
        OutlinedTextField(
            value = chatViewModel.occupation,
            onValueChange = { chatViewModel.occupation = it },
            placeholder = { Text("Ví dụ: sinh viên, kĩ sư,...") },
            modifier = Modifier.fillMaxWidth()
        )

        BotTraitsSection(
            onShowSheet = { showBotTraitsSheet = true },
            showSheet = showBotTraitsSheet,
            onDismissSheet = { showBotTraitsSheet = false }
        )

        OutlinedTextField(
            value = chatViewModel.botTraits,
            onValueChange = { chatViewModel.botTraits = it },
            placeholder = { Text("Hãy mô tả hoặc chọn các đặc điểm mà bạn mong muốn") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 5
        )

        // Thêm hàng ngang các button dưới OutlinedTextField
        BotTraitsButtons(chatViewModel = chatViewModel)

        AdditionalInfoSection(
            onShowSheet = { showAdditionalInfoSheet = true },
            showSheet = showAdditionalInfoSheet,
            onDismissSheet = { showAdditionalInfoSheet = false }
        )

        OutlinedTextField(
            value = chatViewModel.additionalInfo,
            onValueChange = { chatViewModel.additionalInfo = it },
            modifier = Modifier.fillMaxWidth(),
            minLines = 5
        )
    }
}

