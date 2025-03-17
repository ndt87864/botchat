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
fun AdditionalInfoSection(voidChatViewModel: VoidChatViewModel) {
    var showAdditionalInfoSheet by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Text(
            text = "Có yêu cầu nào khác bạn muốn bot biết không?",
            modifier = Modifier
                .clickable { showAdditionalInfoSheet = true }
                .padding(end = 8.dp)
        )
        IconButton(onClick = { showAdditionalInfoSheet = true }) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Thông tin chi tiết"
            )
        }
    }

    OutlinedTextField(
        value = voidChatViewModel.otherRequirements.value,
        onValueChange = { voidChatViewModel.otherRequirements.value = it },
        modifier = Modifier.fillMaxWidth(),
        minLines = 3
    )

    if (showAdditionalInfoSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAdditionalInfoSheet = false },
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Yêu cầu khác",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Bạn có thể cung cấp thêm yêu cầu hoặc sở thích để bot hiểu bạn tốt hơn như..."
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("• Tôi muốn bot nhắc tôi uống nước mỗi giờ")
                Spacer(modifier = Modifier.height(8.dp))
                Text("• Tôi thích bot trả lời ngắn gọn")
                Spacer(modifier = Modifier.height(8.dp))
                Text("• Tôi muốn bot sử dụng tiếng Việt pha chút tiếng Anh")
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { showAdditionalInfoSheet = false },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Đóng")
                }
            }
        }
    }
}