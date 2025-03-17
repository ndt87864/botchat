package com.example.botchat.converstation.ui.historySetting.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import com.example.botchat.converstation.ui.historySetting.handler.SettingsDataHandler
import com.example.botchat.converstation.viewModel.VoidChatViewModel

@Composable
fun SettingsList(
    viewModel: VoidChatViewModel,
    settingsList: List<Pair<String, Map<String, Any>>>,
    onSettingSelected: (String) -> Unit,
    onNavigateToVoiceChat: () -> Unit, // Thêm callback để chuyển sang VoidChatPage
) {
    val context = LocalContext.current
    var showRefreshDialog by remember { mutableStateOf(false) } // State cho dialog hỏi làm mới
    var selectedSettingId by remember { mutableStateOf<String?>(null) } // Lưu settingId được chọn

    if (settingsList.isEmpty()) {
        Text(
            text = "Không có lịch sử thiết lập.",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp),
            textAlign = TextAlign.Center
        )
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(settingsList) { index, (settingId, settingData) ->
                val isDefault = settingData["isDefault"] as? Boolean == true
                val backgroundColor = if (isDefault) Color(0xFFFFF9C4) else if (index == 0) Color(0xFFBBDEFB) else Color(0xFFF1F1F1)
                val borderColor = if (isDefault) Color(0xFFFFC107) else if (index == 0) Color(0xFF1976D2) else Color.Gray
                val borderWidth = if (isDefault || index == 0) 2.dp else 1.dp
                val settingName = settingData["settingName"] as? String
                val displayName = settingName?.takeIf { it.isNotBlank() } ?: settingId
                val userName = settingData["userName"] as? String ?: "Bạn"
                val botName = settingData["botName"] as? String ?: "Bot"
                val displayText = "Người dùng: $userName - Bot: $botName"

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(backgroundColor)
                        .border(width = borderWidth, color = borderColor, shape = RoundedCornerShape(12.dp))
                        .clickable {
                            viewModel.currentEditingSettingId.value = settingId
                            onSettingSelected(settingId)
                        }
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = displayName, style = MaterialTheme.typography.bodyMedium, color = Color.Black)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = displayText, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                        Spacer(modifier = Modifier.weight(1f))

                        var showMenu by remember { mutableStateOf(false) }
                        if (!isDefault) {
                            IconButton(onClick = { showMenu = true }) {
                                Icon(imageVector = Icons.Default.MoreVert, contentDescription = "More Options")
                            }
                        }

                        if (showMenu) {
                            Popup(alignment = Alignment.TopEnd, onDismissRequest = { showMenu = false }) {
                                Box(
                                    modifier = Modifier
                                        .background(Color.White, RoundedCornerShape(8.dp))
                                        .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        TextButton(onClick = {
                                            SettingsDataHandler.deleteSetting(viewModel, settingId, context)
                                            showMenu = false
                                        }) {
                                            Text("Xóa", color = Color.Black)
                                        }
                                        TextButton(onClick = {
                                            selectedSettingId = settingId
                                            showRefreshDialog = true // Hiển thị dialog hỏi làm mới
                                            showMenu = false
                                        }) {
                                            Text("Đặt làm mặc định", color = Color.Black)
                                        }
                                    }
                                }
                            }
                        }

                        if (isDefault) {
                            Icon(
                                imageVector = Icons.Default.PushPin,
                                contentDescription = "Pinned Setting",
                                tint = Color.Black
                            )
                        }
                    }
                }
            }
        }
    }

    // Dialog hỏi làm mới cuộc trò chuyện
    if (showRefreshDialog && selectedSettingId != null) {
        AlertDialog(
            onDismissRequest = { showRefreshDialog = false },
            title = { Text("Bạn có muốn làm mới cuộc trò chuyện ngay bây giờ?") },
            text = { Text("Tính năng này sẽ xóa sạch dữ liệu trò chuyện gần nhất") },
            confirmButton = {
                TextButton(onClick = {
                    SettingsDataHandler.setDefaultSetting(viewModel, selectedSettingId!!, context)
                    // Chuyển sang VoidChatPage
                    onNavigateToVoiceChat()

                    viewModel.conversation.clear()
                    showRefreshDialog = false
                }) {
                    Text("Có")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    SettingsDataHandler.setDefaultSetting(viewModel, selectedSettingId!!, context)
                    // Chỉ đặt làm mặc định, không làm mới ngay
                    showRefreshDialog = false
                }) {
                    Text("Không")
                }
            }
        )
    }
}