package com.example.botchat.converstation.ui.historySetting

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.botchat.converstation.ui.historySetting.components.SettingsList
import com.example.botchat.converstation.ui.historySetting.handler.SettingsDataHandler
import com.example.botchat.converstation.ui.historySetting.widgets.DeleteAllButton
import com.example.botchat.converstation.ui.historySetting.widgets.DeleteAllDialog
import com.example.botchat.converstation.viewModel.VoidChatViewModel

@Composable
fun VoidSettingsHistoryPage(
    viewModel: VoidChatViewModel = viewModel(),
    modifier: Modifier = Modifier,
    onSettingSelected: (String) -> Unit,
    onNavigateToVoiceChat: () -> Unit
) {
    val context = LocalContext.current
    val settingsList = SettingsDataHandler.fetchSettingsRealtime(viewModel)
    var showDeleteAllDialog by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (settingsList.value.isNotEmpty()) {
                Text(
                    text = "Lịch sử thiết lập",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    textAlign = TextAlign.Center
                )
            }
            SettingsList(
                viewModel = viewModel,
                settingsList = settingsList.value,
                onSettingSelected = onSettingSelected,
                onNavigateToVoiceChat = onNavigateToVoiceChat
            )
        }

        DeleteAllDialog(
            showDialog = showDeleteAllDialog,
            onDismiss = { showDeleteAllDialog = false },
            onConfirm = { SettingsDataHandler.deleteAllNonDefaultSettings(viewModel, context) }
        )

        DeleteAllButton(
            onClick = { showDeleteAllDialog = true },
            isVisible = settingsList.value.isNotEmpty(),
            modifier = Modifier.align(Alignment.BottomCenter) // Đặt align ở đây
        )
    }
}