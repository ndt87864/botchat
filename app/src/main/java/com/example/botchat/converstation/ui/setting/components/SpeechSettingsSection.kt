package com.example.botchat.converstation.ui.setting.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.botchat.converstation.viewModel.VoidChatViewModel

@Composable
fun SpeechSettingsSection(voidChatViewModel: VoidChatViewModel) {
    val context = LocalContext.current

    Text("Tốc độ đọc: ${String.format("%.2f", voidChatViewModel.speechRate.value)}")
    Slider(
        value = voidChatViewModel.speechRate.value,
        onValueChange = {
            voidChatViewModel.speechRate.value = it
            voidChatViewModel.applySpeechSettings()
        },
        valueRange = 0.5f..2.0f,
        steps = 29,
        modifier = Modifier.fillMaxWidth()
    )

    Text("Cao độ đọc: ${String.format("%.2f", voidChatViewModel.pitch.value)}")
    Slider(
        value = voidChatViewModel.pitch.value,
        onValueChange = {
            voidChatViewModel.pitch.value = it
            voidChatViewModel.applySpeechSettings()
        },
        valueRange = 0.5f..2.0f,
        steps = 29,
        modifier = Modifier.fillMaxWidth()
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Đọc thử: \"Chào bạn, tôi có thể giúp gì?\"",
            modifier = Modifier.weight(1f)
        )
        IconButton(
            onClick = {
                if (voidChatViewModel.isBotSpeaking.value) {
                    voidChatViewModel.stopBotSpeaking()
                } else {
                    voidChatViewModel.applySpeechSettings()
                    voidChatViewModel.speakBotResponse("Chào bạn, tôi có thể giúp gì cho bạn?", context)
                }
            }
        ) {
            Icon(
                imageVector = if (voidChatViewModel.isBotSpeaking.value) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                contentDescription = null
            )
        }
    }
}