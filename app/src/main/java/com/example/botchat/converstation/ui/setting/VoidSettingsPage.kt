package com.example.botchat.converstation.ui.setting

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.botchat.converstation.ui.setting.components.AdditionalInfoSection
import com.example.botchat.converstation.ui.setting.components.BotTraitsSection
import com.example.botchat.converstation.ui.setting.handler.VoidSettingsHandler
import com.example.botchat.converstation.ui.setting.widgets.BotTraitsButtons
import com.example.botchat.converstation.ui.setting.components.SpeechSettingsSection
import com.example.botchat.converstation.viewModel.VoidChatViewModel
import kotlinx.coroutines.CoroutineScope

@Composable
fun VoidSettingsPage(
    modifier: Modifier = Modifier,
    voidChatViewModel: VoidChatViewModel,
    onNavigateToVoiceChat: () -> Unit,
    coroutineScope: CoroutineScope = rememberCoroutineScope()
) {
    val context = LocalContext.current

    // Gọi các hàm xử lý logic từ VoidSettingsHandler
    VoidSettingsHandler.LoadSettings(voidChatViewModel)
    VoidSettingsHandler.HandleBack(voidChatViewModel, coroutineScope, onNavigateToVoiceChat)
    val saveAction = VoidSettingsHandler.HandleSave(voidChatViewModel, coroutineScope, context, onNavigateToVoiceChat)

    Column(
        modifier = modifier
            .padding(16.dp)
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Cài đặt Bot Chat",
            style = MaterialTheme.typography.headlineSmall
        )

        Text("Tên thiết lập")
        OutlinedTextField(
            value = voidChatViewModel.settingName.value,
            onValueChange = { voidChatViewModel.settingName.value = it },
            modifier = Modifier.fillMaxWidth()
        )

        Text("Bot sẽ gọi bạn là gì?")
        OutlinedTextField(
            value = voidChatViewModel.userName.value,
            onValueChange = { voidChatViewModel.userName.value = it },
            modifier = Modifier.fillMaxWidth()
        )

        Text("Tên của bot là gì?")
        OutlinedTextField(
            value = voidChatViewModel.botName.value,
            onValueChange = { voidChatViewModel.botName.value = it },
            modifier = Modifier.fillMaxWidth()
        )

        SpeechSettingsSection(voidChatViewModel = voidChatViewModel)

        BotTraitsSection(voidChatViewModel = voidChatViewModel)

        BotTraitsButtons(voidChatViewModel = voidChatViewModel)

        AdditionalInfoSection(voidChatViewModel = voidChatViewModel)

        Button(
            onClick = saveAction,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                if (voidChatViewModel.currentEditingSettingId.value != null) "Cập nhật thay đổi"
                else "Thêm thiết lập"
            )
        }
    }
}