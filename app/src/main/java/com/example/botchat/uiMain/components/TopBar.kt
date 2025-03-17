package com.example.botchat.uiMain.components

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.botchat.chatbox.viewModel.ChatViewModel
import com.example.botchat.account.viewModel.AccountViewModel
import com.example.botchat.converstation.viewModel.VoidChatViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    selectedItem: String,
    onItemSelected: (String) -> Unit,
    chatViewModel: ChatViewModel,
    voidChatViewModel: VoidChatViewModel,
    accountViewModel: AccountViewModel,
    drawerState: DrawerState,
    coroutineScope: CoroutineScope,
    isExitSummaryShown: Boolean = false, // Thêm tham số để kiểm tra trạng thái ExitVoidChat
    onExitSummary: () -> Unit = {} // Callback khi nhấn Back ở trạng thái ExitVoidChat
) {
    val isLoggedIn by remember { derivedStateOf { accountViewModel.isUserLoggedIn() } }
    val userName by remember { derivedStateOf { accountViewModel.userName ?: "Đăng nhập/Đăng ký" } }
    var moreMenuExpanded by remember { mutableStateOf(false) }
    var modelMenuExpanded by remember { mutableStateOf(false) }
    var allModelsMenuExpanded by remember { mutableStateOf(false) }
    var voiceMoreMenuExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    var settingsCount by remember { mutableStateOf(0) }

    DisposableEffect(Unit) {
        val userEmail = accountViewModel.userEmail ?: FirebaseAuth.getInstance().currentUser?.email ?: return@DisposableEffect onDispose {}
        val collectionRef = voidChatViewModel.firestore.collection("void_settings").document(userEmail).collection("settings")
        val listener = collectionRef.addSnapshotListener { snapshot, error ->
            if (error == null && snapshot != null) {
                settingsCount = snapshot.size()
            }
        }
        onDispose { listener?.remove() }
    }

    when {
        selectedItem == "Chế độ thoại" && isExitSummaryShown -> {
            // Khi ở trang ExitVoidChat, chỉ hiển thị nút Back
            TopAppBar(
                title = { Text("Tóm tắt cuộc trò chuyện") },
                navigationIcon = {
                    IconButton(onClick = { onExitSummary() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        }
        selectedItem in listOf("Tiện ích", "Tài khoản", "Lịch sử", "Chế độ thoại", "Thêm thiết lập", "Tùy chỉnh", "Cá nhân hóa") -> {
            TopAppBar(
                title = { Text(selectedItem) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (selectedItem == "Chế độ thoại") {
                            if (voidChatViewModel.isBotSpeaking.value) {
                                voidChatViewModel.stopBotSpeaking()
                            }
                            onItemSelected("Chế độ thoại")
                            (context as? Activity)?.onBackPressed()
                        } else if (selectedItem == "Thêm thiết lập") {
                            if (voidChatViewModel.currentEditingSettingId.value == null) {
                                val userEmail = FirebaseAuth.getInstance().currentUser?.email
                                if (userEmail != null) {
                                    coroutineScope.launch {
                                        try {
                                            val snapshot = voidChatViewModel.firestore
                                                .collection("void_settings")
                                                .document(userEmail)
                                                .collection("settings")
                                                .whereEqualTo("isDefault", true)
                                                .get()
                                                .await()
                                            if (!snapshot.isEmpty) {
                                                val doc = snapshot.documents.first()
                                                voidChatViewModel.userName.value = doc.getString("userName") ?: ""
                                                voidChatViewModel.botName.value = doc.getString("botName") ?: ""
                                                voidChatViewModel.speechRate.value = doc.getDouble("speechRate")?.toFloat() ?: 1.0f
                                                voidChatViewModel.pitch.value = doc.getDouble("pitch")?.toFloat() ?: 1.0f
                                                voidChatViewModel.settingName.value = doc.getString("settingName") ?: ""
                                                voidChatViewModel.botCharacteristics.value = doc.getString("botCharacteristics") ?: ""
                                                voidChatViewModel.otherRequirements.value = doc.getString("otherRequirements") ?: ""
                                            }
                                        } catch (e: Exception) {
                                            // Log error nếu cần
                                        } finally {
                                            onItemSelected("Tùy chỉnh")
                                            voidChatViewModel.currentEditingSettingId.value = null
                                        }
                                    }
                                } else {
                                    onItemSelected("Tùy chỉnh")
                                    voidChatViewModel.currentEditingSettingId.value = null
                                }
                            } else {
                                onItemSelected("Tùy chỉnh")
                                voidChatViewModel.currentEditingSettingId.value = null
                            }
                        } else if (selectedItem == "Tùy chỉnh") {
                            onItemSelected("Chế độ thoại")
                        } else {
                            onItemSelected("Chế độ trò chuyện")
                        }
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                actions = {
                    when (selectedItem) {
                        "Chế độ thoại" -> {
                            val isSpeakerOn by voidChatViewModel.isSpeakerOn
                            IconButton(onClick = {
                                voidChatViewModel.toggleSpeaker()
                                Toast.makeText(context, if (isSpeakerOn) "Tắt loa" else "Bật loa", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(
                                    imageVector = if (isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                                    contentDescription = if (isSpeakerOn) "Tắt loa" else "Bật loa"
                                )
                            }
                            IconButton(onClick = { voiceMoreMenuExpanded = !voiceMoreMenuExpanded }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "Tùy chọn khác")
                            }
                            DropdownMenu(
                                expanded = voiceMoreMenuExpanded,
                                onDismissRequest = { voiceMoreMenuExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Tùy chỉnh") },
                                    onClick = {
                                        onItemSelected("Tùy chỉnh")
                                        voiceMoreMenuExpanded = false
                                    }
                                )
                            }
                        }
                        "Tùy chỉnh" -> {
                            if (settingsCount < 10) {
                                IconButton(onClick = { onItemSelected("Thêm thiết lập") }) {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = "Thêm thiết lập")
                                }
                            }
                        }
                        "Cá nhân hóa" -> {
                            IconButton(onClick = {
                                chatViewModel.updateSettings(
                                    chatViewModel.applyCustomization,
                                    chatViewModel.userName,
                                    chatViewModel.occupation,
                                    chatViewModel.botTraits,
                                    chatViewModel.additionalInfo,
                                    context,
                                    onSaveSuccess = { onItemSelected("Chế độ trò chuyện") }
                                )
                            }) {
                                Icon(imageVector = Icons.Default.Done, contentDescription = "Lưu cài đặt")
                            }
                        }
                    }
                }
            )
        }
        selectedItem in listOf("Đăng nhập/Đăng ký", "Quên mật khẩu") -> {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "CHATBOT AI",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            brush = Brush.linearGradient(colors = listOf(Color(0xFFFA740C), Color(0xFF00FF00)))
                        ),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        textAlign = TextAlign.Center,
                        fontSize = 22.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onItemSelected("Chế độ trò chuyện") }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        }
        else -> {
            // Giữ nguyên logic mặc định cho các trang khác
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = "ChatBot", fontSize = 20.sp)
                        if (isLoggedIn) {
                            IconButton(onClick = { modelMenuExpanded = !modelMenuExpanded }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Chọn Model")
                            }
                            DropdownMenu(
                                expanded = modelMenuExpanded,
                                onDismissRequest = { modelMenuExpanded = false }
                            ) {
                                val topModels = chatViewModel.availableModels.take(3)
                                topModels.forEach { customModelName ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(text = customModelName, style = MaterialTheme.typography.bodyLarge)
                                                Text(
                                                    text = getModelDescription(customModelName),
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                                                )
                                            }
                                        },
                                        onClick = {
                                            chatViewModel.changeModel(customModelName)
                                            modelMenuExpanded = false
                                            Toast.makeText(context, "Đã chuyển sang $customModelName", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }
                                if (chatViewModel.availableModels.size > 3) {
                                    DropdownMenuItem(
                                        text = { Text("Xem tất cả") },
                                        onClick = {
                                            modelMenuExpanded = false
                                            allModelsMenuExpanded = true
                                        }
                                    )
                                }
                            }
                            DropdownMenu(
                                expanded = allModelsMenuExpanded,
                                onDismissRequest = { allModelsMenuExpanded = false }
                            ) {
                                chatViewModel.availableModels.forEach { customModelName ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(text = customModelName, style = MaterialTheme.typography.bodyLarge)
                                                Text(
                                                    text = getModelDescription(customModelName),
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                                                )
                                            }
                                        },
                                        onClick = {
                                            chatViewModel.changeModel(customModelName)
                                            allModelsMenuExpanded = false
                                            Toast.makeText(context, "Đã chuyển sang $customModelName", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }
                                DropdownMenuItem(
                                    text = { Text("Thu gọn") },
                                    onClick = {
                                        allModelsMenuExpanded = false
                                        modelMenuExpanded = true
                                    }
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { coroutineScope.launch { drawerState.open() } }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                },
                actions = {
                    if (selectedItem != "Đăng nhập/Đăng ký") {
                        if (isLoggedIn) {
                            IconButton(onClick = {
                                onItemSelected("Tạo phòng")
                                chatViewModel.createNewChatRoom()
                                onItemSelected("Chế độ trò chuyện")
                                Toast.makeText(context, "Phòng đã được tạo", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(imageVector = Icons.Default.PostAdd, contentDescription = "Tạo phòng")
                            }
                            IconButton(onClick = { moreMenuExpanded = !moreMenuExpanded }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "More options")
                            }
                            DropdownMenu(
                                expanded = moreMenuExpanded,
                                onDismissRequest = { moreMenuExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (chatViewModel.isAnonymous) Icons.Default.NoAccounts else Icons.Default.AccountCircle,
                                                contentDescription = "Ẩn danh",
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Text(text = "Ẩn danh")
                                        }
                                    },
                                    onClick = {
                                        chatViewModel.toggleAnonymous()
                                        chatViewModel.createNewChatRoom()
                                        Toast.makeText(
                                            context,
                                            if (chatViewModel.isAnonymous) "Chế độ ẩn danh được bật" else "Chế độ ẩn danh bị tắt",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        moreMenuExpanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.History,
                                                contentDescription = "Lịch sử",
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Text(text = "Lịch sử")
                                        }
                                    },
                                    onClick = {
                                        onItemSelected("Lịch sử")
                                        moreMenuExpanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Settings,
                                                contentDescription = "Cá nhân hóa",
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Text(text = "Cá nhân hóa")
                                        }
                                    },
                                    onClick = {
                                        onItemSelected("Cá nhân hóa")
                                        moreMenuExpanded = false
                                    }
                                )
                            }
                        } else {
                            IconButton(
                                onClick = { onItemSelected("Đăng nhập/Đăng ký") },
                                modifier = Modifier.widthIn(min = 120.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.Person, contentDescription = "User")
                                    Text(
                                        text = userName,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            )
        }
    }
}

fun getModelDescription(modelName: String): String {
    return when (modelName) {
        "Genmini 2" -> "Tối ưu cho công việc hàng ngày"
        "Genmini 2 Thinking" -> "Suy luận đa bước từ dữ liệu tìm kiếm thời gian thực"
        "Genmini 2 Pro" -> "Phiên bản mạnh mẽ nhất của Genmini 2.0"
        "Genmini 1.5 " -> "Phù hợp với yêu cầu cơ bản"
        "Genmini 1.5 Pro" -> "Cải thiện hiệu suất so với Genmini 1.5"
        "Genmini 1.5 Pro Experimental" -> "Phù hợp cho học tập"
        "Genmini 1 Pro" -> "Phiên bản mạnh mẽ nhất của thế hệ đầu tiên"
        else -> "Không có thông tin mô tả"
    }
}