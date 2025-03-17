package com.example.botchat.chatbox.ui.chat.widgets

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun ScrollToBottomButton(listState: LazyListState, coroutineScope: CoroutineScope) {
    val showScrollButton by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 0 }
    }

    if (showScrollButton) {
        Box(
            modifier = Modifier.fillMaxSize(), // Để `align()` hoạt động
            contentAlignment = Alignment.BottomEnd // Căn nút về góc phải dưới
        ) {
            FloatingActionButton(
                onClick = {
                    coroutineScope.launch {
                        listState.animateScrollToItem(0)
                    }
                },
                modifier = Modifier
                    .padding(16.dp)
                    .size(50.dp),
                containerColor = Color.Gray
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowDownward,
                    contentDescription = "Cuộn xuống cuối",
                    tint = Color.White
                )
            }
        }
    }
}
