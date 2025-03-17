package com.example.botchat.account.ui.authentication.forgot

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.botchat.account.viewModel.AccountViewModel
import kotlinx.coroutines.launch

@Composable
fun ForgotPasswordPage(
    viewModel: AccountViewModel,
    modifier: Modifier = Modifier,
    onNavigateToResetPassword: () -> Unit // Giữ tham số này để quay lại trang đăng nhập
) {
    var email by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(66.dp))
        Text(
            text = "LẤY LẠI TÀI KHOẢN",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentWidth(align = androidx.compose.ui.Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            singleLine = true,
            enabled = !isProcessing
        )

        Button(
            onClick = {
                if (email.isNotEmpty()) {
                    isProcessing = true
                    coroutineScope.launch {
                        val exists = viewModel.checkEmailExists(email)
                        if (exists) {
                            viewModel.sendResetPasswordEmail(email)
                            val result = viewModel.resetPassword()
                            when {
                                result.isSuccess -> {
                                    Toast.makeText(context, result.getOrNull() ?: "Đặt lại mật khẩu thành công", Toast.LENGTH_LONG).show()
                                    viewModel.clearCurrentEmail()
                                    onNavigateToResetPassword() // Quay lại trang đăng nhập
                                }
                                result.isFailure -> {
                                    Toast.makeText(context, "Đặt lại mật khẩu thất bại: ${result.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            Toast.makeText(context, "Tài khoản không tồn tại", Toast.LENGTH_SHORT).show()
                        }
                        isProcessing = false
                    }
                } else {
                    Toast.makeText(context, "Vui lòng nhập email", Toast.LENGTH_SHORT).show()
                }
            },
            enabled = !isProcessing,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Xác minh tài khoản")
        }
    }
}