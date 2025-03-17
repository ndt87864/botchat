package com.example.botchat.chatbox.ui.chat.components

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CenterFocusWeak
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.botchat.account.viewModel.AccountViewModel
import com.example.botchat.chatbox.viewModel.ChatViewModel
import com.example.botchat.chatbox.model.MessageModel
import com.example.botchat.chatbox.ui.chat.widgets.RecordingAnimation
import com.example.botchat.ui.theme.Yellow900
import com.google.android.gms.auth.api.signin.GoogleSignIn

@Composable
fun MessageInput(onMessageSend: (String) -> Unit, viewModel: ChatViewModel) {
    val context = LocalContext.current
    val activity = context as? Activity
    val isSearchMode = viewModel.isSearchMode

    val signInLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.handleSignInResult(result.data, context) {
                Toast.makeText(context, "Đăng nhập Google thành công", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Đăng nhập Google thất bại", Toast.LENGTH_SHORT).show()
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            viewModel.selectedImageUri = it.toString()
            viewModel.uploadImageToDrive(it, context, activity) { driveLink ->
                val userMessage = MessageModel(
                    message = "",
                    role = "user",
                    timestamp = System.currentTimeMillis(),
                    driveLink = driveLink
                )
                viewModel.messageList.add(userMessage)
                viewModel.auth.currentUser?.email?.let { email ->
                    viewModel.firestoreHandler.saveMessageToFirestore(userMessage, "user", email, viewModel.roomId)
                }
                if (viewModel.shouldAnalyzeImage) {
                    viewModel.processImageAnalysis(it, context)
                } else if (viewModel.isORC) {
                    viewModel.processImage(it, context)
                }
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        bitmap?.let {
            val uri = bitmapToUri(context, it)
            viewModel.selectedImageUri = uri.toString()
            viewModel.uploadImageToDrive(uri, context, activity) { driveLink ->
                val userMessage = MessageModel(
                    message = "",
                    role = "user",
                    timestamp = System.currentTimeMillis(),
                    driveLink = driveLink
                )
                viewModel.messageList.add(userMessage)
                viewModel.auth.currentUser?.email?.let { email ->
                    viewModel.firestoreHandler.saveMessageToFirestore(userMessage, "user", email, viewModel.roomId)
                }
                if (viewModel.shouldAnalyzeImage) {
                    viewModel.processImageAnalysis(uri, context)
                } else {
                    viewModel.processImage(uri, context)
                }
            }
        }
    }

    var showImageOptions by remember { mutableStateOf(false) }
    var showOCRSubMenu by remember { mutableStateOf(false) }
    var showAnalysisSubMenu by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.initGoogleSignIn(context)
        viewModel.initVoiceRecognition(context, onMessageSend)
    }

    Column {
        if (viewModel.isUploading) {
            LinearProgressIndicator(
                progress = viewModel.uploadProgress,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp)
                    .heightIn(min = 4.dp, max = 8.dp),
                color = Yellow900
            )
            Text(
                text = "Đang tải lên: ${String.format("%.0f", viewModel.uploadProgress * 100)}%",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                color = Color.Gray
            )
        }

        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 150.dp)
                .clip(RoundedCornerShape(24.dp))
                .padding(horizontal = 10.dp),
            value = viewModel.message,
            onValueChange = { viewModel.message = it },
            shape = RoundedCornerShape(24.dp),
            leadingIcon = {
                Row {
                    if (viewModel.currentModelName != "Genmini 2 Thinking") {
                        IconButton(
                            onClick = { viewModel.toggleDeepThinking(context) },
                            enabled = !viewModel.isChatbotResponding
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = "Suy nghĩ sâu",
                                tint = if (viewModel.isDeepThinkingEnabled) Yellow900 else Color.Gray
                            )
                        }
                    }
                    if (AccountViewModel().isUserLoggedIn()) {
                        Box {
                            IconButton(
                                onClick = { showImageOptions = true },
                                enabled = !viewModel.isChatbotResponding && !viewModel.isUploading
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CenterFocusWeak,
                                    contentDescription = "Chọn ảnh"
                                )
                            }
                            DropdownMenu(
                                expanded = showImageOptions,
                                onDismissRequest = { showImageOptions = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("OCR") },
                                    onClick = {
                                        showImageOptions = false
                                        showOCRSubMenu = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Phân tích hình ảnh") },
                                    onClick = {
                                        showImageOptions = false
                                        showAnalysisSubMenu = true
                                    }
                                )
                            }
                            DropdownMenu(
                                expanded = showOCRSubMenu,
                                onDismissRequest = { showOCRSubMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Camera") },
                                    onClick = {
                                        showOCRSubMenu = false
                                        viewModel.shouldAnalyzeImage = false
                                        viewModel.isORC = true
                                        if (GoogleSignIn.getLastSignedInAccount(context) == null && activity != null) {
                                            signInLauncher.launch(viewModel.startGoogleSignIn(activity))
                                        } else if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                                            cameraLauncher.launch()
                                        } else {
                                            ActivityCompat.requestPermissions(
                                                activity ?: return@DropdownMenuItem,
                                                arrayOf(Manifest.permission.CAMERA),
                                                1
                                            )
                                        }
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Chọn ảnh") },
                                    onClick = {
                                        showOCRSubMenu = false
                                        viewModel.shouldAnalyzeImage = false
                                        viewModel.isORC = true
                                        if (GoogleSignIn.getLastSignedInAccount(context) == null && activity != null) {
                                            signInLauncher.launch(viewModel.startGoogleSignIn(activity))
                                        } else {
                                            imagePickerLauncher.launch("image/*")
                                        }
                                    }
                                )
                            }
                            DropdownMenu(
                                expanded = showAnalysisSubMenu,
                                onDismissRequest = { showAnalysisSubMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Camera") },
                                    onClick = {
                                        showAnalysisSubMenu = false
                                        viewModel.shouldAnalyzeImage = true
                                        viewModel.isORC = false
                                        if (GoogleSignIn.getLastSignedInAccount(context) == null && activity != null) {
                                            signInLauncher.launch(viewModel.startGoogleSignIn(activity))
                                        } else if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                                            cameraLauncher.launch()
                                        } else {
                                            ActivityCompat.requestPermissions(
                                                activity ?: return@DropdownMenuItem,
                                                arrayOf(Manifest.permission.CAMERA),
                                                1
                                            )
                                        }
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Chọn ảnh") },
                                    onClick = {
                                        showAnalysisSubMenu = false
                                        viewModel.shouldAnalyzeImage = true
                                        viewModel.isORC = false
                                        if (GoogleSignIn.getLastSignedInAccount(context) == null && activity != null) {
                                            signInLauncher.launch(viewModel.startGoogleSignIn(activity))
                                        } else {
                                            imagePickerLauncher.launch("image/*")
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            },
            trailingIcon = {
                Row {
                    IconButton(
                        onClick = { viewModel.toggleSearchMode(context) },
                        enabled = !viewModel.isChatbotResponding
                    ) {
                        Icon(
                            imageVector = if (isSearchMode) Icons.Default.Search else Icons.Default.SearchOff,
                            contentDescription = "Toggle Search Mode"
                        )
                    }
                    if (viewModel.isChatbotResponding) {
                        IconButton(
                            onClick = {
                                viewModel.stopTyping()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = "Stop Typing"
                            )
                        }
                    } else if (viewModel.message.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                if (!isNetworkAvailable(context)) {
                                    Toast.makeText(context, "Không có kết nối internet", Toast.LENGTH_SHORT).show()
                                } else {
                                    onMessageSend(viewModel.message)
                                    viewModel.message = ""
                                }
                            },
                            enabled = viewModel.message.isNotEmpty() && !viewModel.isChatbotResponding
                        ) {
                            Icon(imageVector = Icons.Default.Send, contentDescription = "Send")
                        }
                    } else {
                        IconButton(
                            onClick = {
                                if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                                    if (!viewModel.isRecording) {
                                        viewModel.isRecording = true
                                        Toast.makeText(context, "Đang thu âm", Toast.LENGTH_SHORT).show()
                                        viewModel.startVoiceRecognition()
                                    } else {
                                        viewModel.stopVoiceRecognition()
                                    }
                                } else {
                                    ActivityCompat.requestPermissions(
                                        activity ?: return@IconButton,
                                        arrayOf(Manifest.permission.RECORD_AUDIO),
                                        0
                                    )
                                }
                            },
                            enabled = !viewModel.isChatbotResponding
                        ) {
                            Icon(
                                imageVector = if (viewModel.isRecording) Icons.Default.StopCircle else Icons.Default.Mic,
                                contentDescription = if (viewModel.isRecording) "Stop Recording" else "Micro"
                            )
                        }
                    }
                }
            }
        )

        if (viewModel.isRecording) {
            RecordingAnimation()
        }
    }
}

// Các hàm hỗ trợ giữ nguyên
private fun bitmapToUri(context: Context, bitmap: android.graphics.Bitmap): Uri {
    val bytes = java.io.ByteArrayOutputStream()
    bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 100, bytes)
    val path = android.provider.MediaStore.Images.Media.insertImage(
        context.contentResolver,
        bitmap,
        "Image_${System.currentTimeMillis()}",
        null
    )
    return Uri.parse(path)
}

fun isNetworkAvailable(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetwork = connectivityManager.activeNetwork
    val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
    return networkCapabilities != null && networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}