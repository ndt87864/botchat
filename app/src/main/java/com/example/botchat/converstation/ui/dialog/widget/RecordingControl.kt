package com.example.botchat.converstation.ui.dialog.widget

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.botchat.converstation.viewModel.VoidChatViewModel

@Composable
fun RecordingControl(
    viewModel: VoidChatViewModel,
    onExitClick: () -> Unit   // Callback cho nút thoát
) {
    val isRecording = viewModel.isRecording.value
    val isBotSpeaking = viewModel.isBotSpeaking.value
    val context = LocalContext.current
    val activity = context as? Activity
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Nút ghi âm
        Button(
            onClick = {
                if (isRecording) {
                    viewModel.stopRecording()
                } else {
                    viewModel.startRecording(context)
                }

                if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                    if (isRecording) {
                        viewModel.stopRecording()
                    } else {
                        viewModel.startRecording(context)
                    }
                } else {
                    ActivityCompat.requestPermissions(
                        activity ?: return@Button,
                        arrayOf(Manifest.permission.RECORD_AUDIO),
                        0
                    )
                }
            },
            enabled = !isBotSpeaking,
            modifier = Modifier
                .size(80.dp)
                .background(
                    color = if (isRecording) Color.Red else Color.Gray,
                    shape = CircleShape
                ),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
        ) {
            Icon(
                imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                contentDescription = if (isRecording) "Dừng ghi âm" else "Bắt đầu ghi âm",
                tint = Color.White,
                modifier = Modifier.size(40.dp)
            )
        }

        // Nút thoát: Tắt đọc trước khi gọi callback onExitClick
        IconButton(
            onClick = {
                if (isBotSpeaking) {
                    viewModel.stopBotSpeaking() // Tắt đọc nếu bot đang nói
                }
                onExitClick() // Gọi callback để hiển thị tóm tắt
            },
            modifier = Modifier
                .size(80.dp)
                .background(
                    color = Color.Gray,
                    shape = CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Thoát",
                tint = Color.White
            )
        }
    }

    // Hiển thị trạng thái hành động
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = when {
                isBotSpeaking -> "Bot đang nói..."
                isRecording -> "Đang ghi âm..."
                else -> "Nhấn vào mic để bắt đầu nói"
            },
            fontSize = 16.sp,
            color = Color.Gray
        )
    }
}