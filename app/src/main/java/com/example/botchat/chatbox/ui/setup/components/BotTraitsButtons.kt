package com.example.botchat.chatbox.ui.setup.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.botchat.chatbox.viewModel.ChatViewModel

@Composable
fun BotTraitsButtons(chatViewModel: ChatViewModel) {
    val traitsMap = mapOf(
        "Hoạt ngôn" to "Trò chuyện nhiều và đa dạng.",
        "Hóm hỉnh" to "Sử dụng sự hài hước nhanh nhạy và thông minh trong hoàn cảnh thích hợp.",
        "Thẳng thắn" to "Nói thẳng, không vòng vo hay che đậy.",
        "Nói chuyện như Gen Z" to "Nói chuyện như Gen Z thứ thiệt.",
        "Thể hiện phong cách hoài nghi" to "Thể hiện phong cách hoài nghi, chất vấn.",
        "Giữ quan điểm truyền thống" to "Giữ quan điểm truyền thống, đề cao các giá trị và cách thức làm việc truyền thống.",
        "Tầm nhìn xa" to "Thể hiện tầm nhìn xa trông rộng.",
        "Ngôn ngữ giàu cảm xúc" to "Sử dụng ngôn ngữ đầy chất thơ và giàu cảm xúc.",
        "Quan điểm mạnh mẽ" to "Sẵn sàng bày tỏ những quan điểm mạnh mẽ.",
        "Tôn trọng" to "Luôn thể hiện sự tôn trọng.",
        "Khiêm nhường" to "Thể hiện sự khiêm nhường trong hoàn cảnh thích hợp.",
        "Chuyên nghiệp" to "Sử dụng giọng điệu chuyên nghiệp và trang trọng.",
        "Vui nhộn" to "Thể hiện sự vui nhộn và tinh nghịch.",
        "Đi thẳng vào vấn đề" to "Đi thẳng vào vấn đề.",
        "Thực tế" to "Ưu tiên tính thực tế.",
        "Công sở" to "Trò chuyện theo phong cách công sở.",
        "Nhẹ nhàng" to "Trò chuyện nhẹ nhàng và thoải mái.",
        "Sáng tạo" to "Sáng tạo và tư duy khác biệt.",
        "Đồng cảm" to "Thể hiện sự đồng cảm và thấu hiểu trong các phản hồi."
    )

    val traitsList = traitsMap.keys.toList()
    val visibleButtons = remember { mutableStateMapOf(*traitsList.map { it to true }.toTypedArray()) }

    // Theo dõi sự thay đổi của botTraits để cập nhật lại trạng thái button
    LaunchedEffect(chatViewModel.botTraits) {
        traitsList.forEach { trait ->
            val traitDescription = traitsMap[trait] ?: trait
            visibleButtons[trait] = !chatViewModel.botTraits.contains(traitDescription, ignoreCase = true)
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 8.dp)
    ) {
        traitsList.forEach { trait ->
            if (visibleButtons[trait] == true) {
                OutlinedButton(
                    onClick = {
                        // Thêm mô tả vào botTraits
                        val currentTraits = chatViewModel.botTraits
                        chatViewModel.botTraits = if (currentTraits.isEmpty()) {
                            traitsMap[trait] ?: trait
                        } else {
                            "$currentTraits ${traitsMap[trait] ?: trait}"
                        }
                        // Ẩn button ngay sau khi nhấn
                        visibleButtons[trait] = false
                    },
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(0.dp, Color.Transparent),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text("+ $trait")
                }
            }
        }
    }
}
