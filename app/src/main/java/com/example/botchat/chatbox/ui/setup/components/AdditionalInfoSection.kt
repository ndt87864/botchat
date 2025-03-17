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
fun AdditionalInfoSection(
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
            text = "Có điều gì khác bạn muốn chatbot biết không?",
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
                    text = "Thông tin về bạn",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentWidth(align = Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Bạn có thể cung cấp thêm thông tin cá nhân hoặc sở thích để ChatBot hiểu bạn tốt hơn như..."
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = " • Tôi thích đi bộ đường dài và nghe nhạc vpop"
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = " • Tôi thích xem phim và nghe nhạc"
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = " • Tôi đang học tiếng Anh"
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