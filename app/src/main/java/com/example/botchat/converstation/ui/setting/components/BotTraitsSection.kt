package com.example.botchat.converstation.ui.setting.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.botchat.converstation.viewModel.VoidChatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BotTraitsSection(voidChatViewModel: VoidChatViewModel) {
    var showBotTraitsSheet by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Text(
            text = "Bot nên có những đặc điểm gì?",
            modifier = Modifier
                .clickable { showBotTraitsSheet = true }
                .padding(end = 8.dp)
        )
        IconButton(onClick = { showBotTraitsSheet = true }) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Thông tin chi tiết"
            )
        }
    }

    OutlinedTextField(
        value = voidChatViewModel.botCharacteristics.value,
        onValueChange = { voidChatViewModel.botCharacteristics.value = it },
        placeholder = { Text("Mô tả hoặc chọn đặc điểm mong muốn") },
        modifier = Modifier.fillMaxWidth(),
        minLines = 3
    )

    if (showBotTraitsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBotTraitsSheet = false },
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Đặc điểm của Bot",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Bot có thể được tùy chỉnh với các đặc điểm như:")
                Spacer(modifier = Modifier.height(8.dp))
                Text("• Năng động và nhiệt tình")
                Text("• Hóm hỉnh và vui vẻ")
                Text("• Chuyên nghiệp và nghiêm túc")
                Text("• Thân thiện và nhẹ nhàng")
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { showBotTraitsSheet = false },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Đóng")
                }
            }
        }
    }
}