package com.example.botchat.chatbox.ui.chat.handlers

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.core.content.ContextCompat
import com.example.botchat.chatbox.viewModel.ChatViewModel
import com.example.botchat.chatbox.model.UiEvent

@Composable
fun HandleUiEvents(
    viewModel: ChatViewModel,
    context: android.content.Context,
    callPermissionLauncher: androidx.activity.compose.ManagedActivityResultLauncher<String, Boolean>,
    lastCallNumber: String?,
    updateLastCallNumber: (String) -> Unit
) {
    LaunchedEffect(viewModel) {
        viewModel.event.collect { event ->
            when (event) {
                is UiEvent.MakePhoneCall -> {
                    updateLastCallNumber(event.number)
                    val permissionGranted = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.CALL_PHONE
                    ) == PackageManager.PERMISSION_GRANTED

                    if (permissionGranted) {
                        startCall(context, event.number)
                    } else {
                        // Luôn yêu cầu quyền nếu chưa được cấp
                        callPermissionLauncher.launch(Manifest.permission.CALL_PHONE)
                        // Thông báo cho người dùng (tùy chọn)
                        Toast.makeText(
                            context,
                            "Vui lòng cấp quyền gọi điện để thực hiện cuộc gọi",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                is UiEvent.OpenDialer -> {
                    openDialer(context, event.number)
                }
            }
        }
    }
}

fun startCall(context: android.content.Context, number: String) {
    try {
        val intent = Intent(Intent.ACTION_CALL).apply {
            data = Uri.parse("tel:$number")
        }
        context.startActivity(intent)
    } catch (e: SecurityException) {
        // Xử lý trường hợp không có quyền (dù đã kiểm tra trước đó)
        Toast.makeText(context, "Không có quyền thực hiện cuộc gọi", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Lỗi khi thực hiện cuộc gọi: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

fun openDialer(context: android.content.Context, number: String) {
    val intent = Intent(Intent.ACTION_DIAL).apply {
        data = Uri.parse("tel:$number")
    }
    context.startActivity(intent)
}