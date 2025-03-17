package com.example.botchat.extends

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.example.botchat.R
import com.example.botchat.chatbox.viewModel.ChatViewModel

@Composable
fun ExtendsPage(modifier: Modifier = Modifier, viewModel: ChatViewModel) {
    Box(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            YouTubeModeToggle(viewModel = viewModel)
            CallModeToggle(viewModel = viewModel)
            SmsModeToggle(viewModel = viewModel)
        }
    }
}

@Composable
fun CallModeToggle(viewModel: ChatViewModel) {
    val isCallMode = viewModel.isCallMode
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(Color.LightGray.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.call), // Ensure this resource exists
                        contentDescription = "Call Icon",
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Chế độ gọi điện",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Switch(
                    checked = isCallMode,
                    onCheckedChange = { viewModel.toggleCallMode(context) }
                )
            }
            Text(
                text = "Bật chế độ này để cho phép ứng dụng thực hiện cuộc gọi/ quay số  .",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                text = "Ví dụ: Gọi số 123456789.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
fun YouTubeModeToggle(viewModel: ChatViewModel) {
    val isYoutubeMode = viewModel.isYoutubeMode
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(Color.LightGray.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.youtube), // Ensure this resource exists
                        contentDescription = "YouTube Icon",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Chế độ tìm kiếm qua YouTube",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Switch(
                    checked = isYoutubeMode,
                    onCheckedChange = { viewModel.toggleYoutubeMode(context) }
                )
            }
            Text(
                text = "Kích hoạt để tìm kiếm thông tin/ xem video YouTube.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                text = "Ví dụ: Tìm video về nấu ăn ",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
fun SmsModeToggle(viewModel: ChatViewModel) {
    val isSmsMode = viewModel.isMessageMode
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(Color.LightGray.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.sms), // Resource icon for SMS
                        contentDescription = "SMS Icon",
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Chế độ gửi SMS",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Switch(
                    checked = isSmsMode,
                    onCheckedChange = { viewModel.toggleMessageMode(context) }
                )
            }
            Text(
                text = "Bật chế độ này để ứng dụng có thể gửi tin nhắn SMS .",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                text = "Ví dụ: Gửi tin nhắn đến 191 nội dung KTTK.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}