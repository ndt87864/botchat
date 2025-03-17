package com.example.botchat.chatbox.ui.chat.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun RecordingAnimation() {
    // Tạo biến điều khiển chỉ số điểm và khoảng cách giữa các điểm
    var dotIndex by remember { mutableStateOf(0) }

    // Cập nhật vị trí các dấu chấm
    LaunchedEffect(true) {
        while (true) {
            dotIndex = (dotIndex + 1) % 3 // Chuyển đổi giữa ba dấu chấm
            delay(500) // Mỗi 500ms chuyển sang dấu chấm tiếp theo
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .padding(10.dp),
        contentAlignment = Alignment.Center
    ) {
        // Vẽ ba dấu chấm nhấp nháy liên tục
        Row(horizontalArrangement = Arrangement.Center) {
            repeat(3) { index ->
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .padding(4.dp)
                        .clip(CircleShape)
                        .background(
                            color = if (index == dotIndex) Color.Cyan else Color.Gray,
                            shape = CircleShape
                        )
                )
            }
        }
    }
}

