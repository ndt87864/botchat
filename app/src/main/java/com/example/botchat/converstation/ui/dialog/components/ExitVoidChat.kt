package com.example.botchat.converstation.ui.dialog.components

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.botchat.chatbox.viewModel.ChatViewModel

@Composable
fun ExitVoidChat(
    conversation: List<Pair<String, String>>,
    onExit: () -> Unit,
    onSave: () -> Unit
) {
    // Theme-related colors giống MessageRow
    val isDarkTheme = isSystemInDarkTheme()
    val defaultTextColor = if (isDarkTheme) Color.White else Color.Black
    val bubbleBackground = if (isDarkTheme) Color.DarkGray else Color.LightGray
    val cardBackground = if (isDarkTheme) Color(0xFF2A2A2A) else Color(0xFFF5F5F5)

    // Giả định ChatViewModel được truyền vào để truy cập thời gian ghi âm (nếu cần)
    val chatViewModel: ChatViewModel = viewModel()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Tiêu đề "Tóm tắt cuộc trò chuyện"
        Text(
            text = "Tóm tắt cuộc trò chuyện",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = defaultTextColor,
            modifier = Modifier
                .padding(bottom = 16.dp)
                .align(Alignment.CenterHorizontally)
        )

        // Danh sách tin nhắn dưới dạng thẻ cuộn
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()) // Thêm khả năng cuộn
                .padding(8.dp)
        ) {
            conversation.forEachIndexed { index, (userMsg, botMsg) ->
                // Mỗi cặp tin nhắn (user và bot) là một thẻ
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBackground),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                    ) {
                        // Tin nhắn của người dùng
                        if (userMsg.isNotBlank()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 4.dp),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Bạn",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Gray
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(bubbleBackground)
                                            .padding(12.dp)
                                            .width(250.dp)
                                    ) {
                                        Text(
                                            text = userMsg,
                                            fontSize = 16.sp,
                                            color = defaultTextColor
                                        )
                                    }
                                    Row(
                                        modifier = Modifier
                                            .padding(top = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Mic,
                                            contentDescription = "Mic",
                                            tint = defaultTextColor,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Tin nhắn của bot
                        if (botMsg.isNotBlank()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.Start
                            ) {
                                Column {
                                    Text(
                                        text = "Bot",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Gray
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(bubbleBackground)
                                            .padding(12.dp)
                                            .width(250.dp)
                                    ) {
                                        Text(
                                            text = botMsg,
                                            fontSize = 16.sp,
                                            color = defaultTextColor
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Nút hành động
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = { onExit() }) {
                Text(text = "Thoát")
            }
            Button(onClick = { onSave() }) {
                Text(text = "Lưu")
            }
        }
    }
}