package com.example.botchat.chatbox.ui.setup.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BotTraitsSection(
    onShowSheet: () -> Unit,
    showSheet: Boolean,
    onDismissSheet: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Text(
            text = "ChatBot nên có những đặc điểm gì?",
            modifier = Modifier
                .clickable { onShowSheet() }
                .padding(end = 8.dp)
        )
        IconButton(onClick = { onShowSheet() }) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Thông tin chi tiết"

            )
        }
    }

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { onDismissSheet() },
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Đặc điển về ChatBot",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentWidth(align = Alignment.CenterHorizontally)

                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "ChatBot cần bạn nhập mô tả hoặc chọn các đặc điểm mong muốn để cá nhân hóa trải nghiệm như..."
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = " • Sử dụng giọng điệu chuyên nghiệp và trang trọng."
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = " • Sử dụng giọng điệu thoải mái và hoạt ngôn."
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = " • Hãy tích cực nêu ý kiến . Nếu 1 câu hỏi có nhiều " +
                            "\n câu trả lời , hãy cố gắng đưa ra câu trả lời hay " +
                            "\n nhất"
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { onDismissSheet() },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Đóng")
                }
            }
        }
    }
}