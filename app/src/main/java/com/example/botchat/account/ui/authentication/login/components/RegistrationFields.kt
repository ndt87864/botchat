package com.example.botchat.account.ui.authentication.login.components

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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun RegistrationFields(
    username: String,
    onUsernameChange: (String) -> Unit,
    dateOfBirth: String,
    onDateOfBirthChange: (String) -> Unit,
    onDatePickerClick: () -> Unit
) {
    OutlinedTextField(
        value = username,
        onValueChange = onUsernameChange,
        label = { Text("Tên người dùng") },
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
        singleLine = true
    )
    OutlinedTextField(
        value = dateOfBirth,
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
                onDateOfBirthChange(formatted)
            }
        },
        label = { Text("Ngày sinh (dd/mm/yyyy)") },
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Next
        ),
        placeholder = { Text("Ví dụ: 01/01/2000") },
        trailingIcon = {
            IconButton(onClick = onDatePickerClick) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = "Chọn ngày",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    )
}