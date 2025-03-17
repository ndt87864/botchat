package com.example.botchat.account.ui.edit.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun UpdateButton(onClick: () -> Unit, isProcessing: Boolean) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        enabled = !isProcessing
    ) {
        Text("Cập nhật thông tin")
    }
}
