package com.example.botchat.account.ui.edit

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.botchat.account.ui.edit.components.ErrorMessage
import com.example.botchat.account.ui.edit.components.Header
import com.example.botchat.account.ui.edit.components.PasswordFields
import com.example.botchat.account.ui.edit.components.UpdateButton
import com.example.botchat.account.ui.edit.components.UserInfoFields
import com.example.botchat.account.viewModel.AccountViewModel
import kotlinx.coroutines.launch
import java.util.Calendar

@Composable
fun EditAccountPage(
    viewModel: AccountViewModel,
    modifier: Modifier = Modifier,
    onNavigateToChat: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var dateOfBirth by remember { mutableStateOf("") }
    var newUsername by remember { mutableStateOf("") }
    var newDateOfBirth by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var currentPassword by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }
    var isCurrentPasswordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }
    var isChangingPassword by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (viewModel.isUserLoggedIn()) {
            username = viewModel.getUserName() ?: ""
            email = viewModel.getUserEmail() ?: ""
            dateOfBirth = viewModel.getUserDateOfBirth() ?: ""
            newUsername = username
            newDateOfBirth = dateOfBirth
        } else {
            errorMessage = "Bạn cần đăng nhập để chỉnh sửa thông tin"
        }
    }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val calendar = Calendar.getInstance()
    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val formattedDate = String.format("%02d/%02d/%d", dayOfMonth, month + 1, year)
                newDateOfBirth = formattedDate
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            datePicker.maxDate = System.currentTimeMillis()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Header()
        Spacer(modifier = Modifier.height(16.dp))
        UserInfoFields(
            newUsername = newUsername,
            onNewUsernameChange = { newUsername = it },
            newEmail = email, // Sử dụng email gốc, không cần newEmail
            onNewEmailChange = {}, // Không cần thay đổi email qua UI
            newDateOfBirth = newDateOfBirth,
            onNewDateOfBirthChange = { newDateOfBirth = it },
            onDatePickerClick = { datePickerDialog.show() },
            isProcessing = isProcessing
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Checkbox(
                checked = isChangingPassword,
                onCheckedChange = { isChangingPassword = it },
                enabled = !isProcessing
            )
            Text("Thay đổi mật khẩu")
        }

        // Chỉ hiển thị CurrentPasswordField khi thay đổi mật khẩu
        if (isChangingPassword) {
            PasswordFields(
                currentPassword = currentPassword,
                onCurrentPasswordChange = { currentPassword = it },
                isCurrentPasswordVisible = isCurrentPasswordVisible,
                onCurrentPasswordVisibilityChange = { isCurrentPasswordVisible = it },
                password = password,
                onPasswordChange = { password = it },
                isPasswordVisible = isPasswordVisible,
                onPasswordVisibilityChange = { isPasswordVisible = it },
                confirmPassword = confirmPassword,
                onConfirmPasswordChange = { confirmPassword = it },
                isConfirmPasswordVisible = isConfirmPasswordVisible,
                onConfirmPasswordVisibilityChange = { isConfirmPasswordVisible = it },
                isProcessing = isProcessing
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        ErrorMessage(errorMessage = errorMessage)
        Spacer(modifier = Modifier.height(16.dp))
        UpdateButton(
            onClick = {
                if (isProcessing) return@UpdateButton
                if (!viewModel.isUserLoggedIn()) {
                    errorMessage = "Bạn cần đăng nhập để chỉnh sửa thông tin"
                    return@UpdateButton
                }
                if (newUsername.isEmpty() || newDateOfBirth.isEmpty()) {
                    errorMessage = "Tên người dùng và Ngày sinh không được để trống."
                } else if (password.isNotEmpty() && password != confirmPassword) {
                    errorMessage = "Mật khẩu và nhập lại mật khẩu không khớp."
                } else if (!newDateOfBirth.matches("\\d{2}/\\d{2}/\\d{4}".toRegex())) {
                    errorMessage = "Ngày sinh không đúng định dạng (dd/mm/yyyy)"
                } else if (password.isNotEmpty() && currentPassword.isEmpty()) {
                    errorMessage = "Vui lòng nhập mật khẩu hiện tại để thay đổi mật khẩu"
                } else {
                    isProcessing = true
                    coroutineScope.launch {
                        try {
                            val result = viewModel.updateUserInfo(newUsername, email, currentPassword, password, newDateOfBirth)
                            result.onSuccess {
                                username = newUsername
                                dateOfBirth = newDateOfBirth
                                errorMessage = ""
                                Toast.makeText(context, "Cập nhật thông tin thành công!", Toast.LENGTH_SHORT).show()
                                onNavigateToChat()
                            }.onFailure { e ->
                                errorMessage = e.message ?: "Có lỗi xảy ra khi cập nhật thông tin."
                            }
                        } catch (e: Exception) {
                            errorMessage = e.message ?: "Có lỗi xảy ra khi cập nhật thông tin."
                        } finally {
                            isProcessing = false
                        }
                    }
                }
            },
            isProcessing = isProcessing
        )
    }

    // Xóa mật khẩu khi không thay đổi
    LaunchedEffect(isChangingPassword) {
        if (!isChangingPassword) {
            password = ""
            confirmPassword = ""
        }
    }
}