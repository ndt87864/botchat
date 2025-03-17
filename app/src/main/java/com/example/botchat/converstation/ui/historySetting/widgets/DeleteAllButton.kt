package com.example.botchat.converstation.ui.historySetting.widgets

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun DeleteAllButton(
    onClick: () -> Unit,
    isVisible: Boolean,
    modifier: Modifier = Modifier // Thêm Modifier làm tham số
) {
    if (isVisible) {
        IconButton(
            onClick = onClick,
            modifier = modifier
                .padding(bottom = 24.dp)
                .size(64.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Menu xóa lịch sử",
                tint = Color.Red,
                modifier = Modifier.size(35.dp)
            )
        }
    }
}