package com.example.botchat.converstation.ui.setting.widgets

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
import com.example.botchat.converstation.viewModel.VoidChatViewModel

@Composable
fun BotTraitsButtons(voidChatViewModel: VoidChatViewModel) {
    val traitsMap = mapOf(
        "Năng động" to "Năng động và nhiệt tình",
        "Hóm hỉnh" to "Hóm hỉnh và vui vẻ",
        "Chuyên nghiệp" to "Chuyên nghiệp và nghiêm túc",
        "Thân thiện" to "Thân thiện và nhẹ nhàng",
        "Thẳng thắn" to "Nói thẳng, không vòng vo",
        "Sáng tạo" to "Sáng tạo và tư duy khác biệt"
    )

    val traitsList = traitsMap.keys.toList()
    val visibleButtons = remember { mutableStateMapOf(*traitsList.map { it to true }.toTypedArray()) }

    LaunchedEffect(voidChatViewModel.botCharacteristics.value) {
        traitsList.forEach { trait ->
            val traitDescription = traitsMap[trait] ?: trait
            visibleButtons[trait] = !voidChatViewModel.botCharacteristics.value.contains(traitDescription, ignoreCase = true)
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
                        val currentTraits = voidChatViewModel.botCharacteristics.value
                        voidChatViewModel.botCharacteristics.value = if (currentTraits.isEmpty()) {
                            traitsMap[trait] ?: trait
                        } else {
                            "$currentTraits, ${traitsMap[trait] ?: trait}"
                        }
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