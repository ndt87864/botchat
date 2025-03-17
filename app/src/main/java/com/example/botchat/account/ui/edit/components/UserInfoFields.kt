package com.example.botchat.account.ui.edit.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun UserInfoFields(
    newUsername: String,
    onNewUsernameChange: (String) -> Unit,
    newEmail: String,
    onNewEmailChange: (String) -> Unit,
    newDateOfBirth: String,
    onNewDateOfBirthChange: (String) -> Unit,
    onDatePickerClick: () -> Unit,
    isProcessing: Boolean
) {
    OutlinedTextField(
        value = newUsername,
        onValueChange = onNewUsernameChange,
        label = { Text("Tên người dùng") },
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
        singleLine = true,
        enabled = !isProcessing
    )

    OutlinedTextField(
        value = newEmail,
        onValueChange = onNewEmailChange,
        label = { Text("Email") },
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
        singleLine = true,
        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Email),
        enabled = !isProcessing,
        readOnly = true
    )

    OutlinedTextField(
        value = newDateOfBirth,
        onValueChange = { newValue ->
            val digitsOnly = newValue.filter { it.isDigit() }
            var formatted = ""
            when {
                digitsOnly.length <= 2 -> formatted = digitsOnly
                digitsOnly.length <= 4 -> formatted = "${digitsOnly.take(2)}/${digitsOnly.drop(2)}"
                digitsOnly.length <= 8 -> formatted = "${digitsOnly.take(2)}/${digitsOnly.substring(2, 4)}/${digitsOnly.drop(4)}"
                else -> return@OutlinedTextField
            }
            if (formatted.length <= 10) {
                onNewDateOfBirthChange(formatted)
            }
        },
        label = { Text("Ngày sinh (dd/mm/yyyy)") },
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        placeholder = { Text("Ví dụ: 01/01/2000") },
        trailingIcon = {
            IconButton(onClick = onDatePickerClick) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = "Chọn ngày",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        },
        enabled = !isProcessing
    )
}