package com.example.botchat.account.ui.authentication.login.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ActionButton(
    isRegistering: Boolean,
    isProcessing: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .border(2.dp, if (isRegistering) Color.LightGray else Color.DarkGray, shape = RoundedCornerShape(30.dp)),
        enabled = !isProcessing,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isRegistering) Color.LightGray else Color.DarkGray,
            contentColor = if (isRegistering) Color.Black else Color.White
        )
    ) {
        Text(
            text = if (isRegistering) "Đăng ký tài khoản" else "Đăng nhập",
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp)
        )
    }
}