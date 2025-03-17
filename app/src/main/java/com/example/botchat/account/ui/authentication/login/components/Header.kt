package com.example.botchat.account.ui.authentication.login.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp

@Composable
fun Header(isRegistering: Boolean) {
    Text(
        text = if (isRegistering) "ĐĂNG KÍ TÀI KHOẢN" else "ĐĂNG NHẬP TÀI KHOẢN",
        style = MaterialTheme.typography.titleLarge.copy(fontSize = 28.sp),
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentWidth(align = Alignment.CenterHorizontally)
    )
}