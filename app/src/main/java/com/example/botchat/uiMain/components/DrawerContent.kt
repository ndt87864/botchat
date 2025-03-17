package com.example.botchat.uiMain.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.botchat.chatbox.viewModel.ChatViewModel
import com.example.botchat.account.viewModel.AccountViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
// Giao diện thanh công cụ
@Composable
fun DrawerContent(
    drawerState: DrawerState,
    selectedItem: String,
    onItemSelected: (String) -> Unit,
    chatViewModel: ChatViewModel,
    accountViewModel: AccountViewModel,
    coroutineScope: CoroutineScope
) {
    val isLoggedIn by remember { derivedStateOf { accountViewModel.isUserLoggedIn() } }
    val userName by remember { derivedStateOf { accountViewModel.userName ?: "Đăng nhập/Đăng ký" } }
    var showUserSubItems by remember { mutableStateOf(false) }
    val context = LocalContext.current

    ModalDrawerSheet(
        modifier = Modifier.background(MaterialTheme.colorScheme.background)
    ) {
        Text(
            text = "Công Cụ",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(16.dp)
        )
        NavigationDrawerItem(
            label = { Text("Chế độ trò chuyện") },
            icon = { Icon(Icons.Default.Chat, contentDescription = null) },
            selected = selectedItem == "Chế độ trò chuyện",
            onClick = {
                onItemSelected("Chế độ trò chuyện")
                coroutineScope.launch { drawerState.close() }
            },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )
        if (isLoggedIn) {
            NavigationDrawerItem(
                label = { Text("Chế độ thoại") },
                icon = { Icon(Icons.Default.BubbleChart, contentDescription = null) },
                selected = selectedItem == "Chế độ thoại",
                onClick = {
                    onItemSelected("Chế độ thoại")
                    coroutineScope.launch { drawerState.close() }
                },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )
            NavigationDrawerItem(
                label = { Text("Tiện ích") },
                icon = { Icon(Icons.Default.Extension, contentDescription = null) },
                selected = selectedItem == "Tiện ích",
                onClick = {
                    onItemSelected("Tiện ích")
                    coroutineScope.launch { drawerState.close() }
                },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )
            NavigationDrawerItem(
                label = {
                    Row(
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Person, contentDescription = "User")
                        Text(
                            text = userName,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                selected = false,
                onClick = { showUserSubItems = !showUserSubItems },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )
            if (showUserSubItems) {
                NavigationDrawerItem(
                    label = { Text("Sửa tài khoản") },
                    icon = { Icon(Icons.Default.EditNote, contentDescription = null) },
                    selected = selectedItem == "Tài khoản",
                    onClick = {
                        showUserSubItems = !showUserSubItems
                        onItemSelected("Tài khoản")
                        coroutineScope.launch { drawerState.close() }
                    },
                    modifier = Modifier
                        .padding(NavigationDrawerItemDefaults.ItemPadding)
                        .padding(start = 32.dp)
                )
                NavigationDrawerItem(
                    label = { Text("Đăng xuất") },
                    icon = { Icon(Icons.Default.Login, contentDescription = null) },
                    selected = selectedItem == "Đăng xuất",
                    onClick = {
                        showUserSubItems = !showUserSubItems
                        accountViewModel.logoutUser(context)
                        Toast.makeText(context, "Đăng xuất thành công", Toast.LENGTH_SHORT).show()
                        onItemSelected("Đăng nhập/Đăng ký")
                        coroutineScope.launch { drawerState.close() }
                    },
                    modifier = Modifier
                        .padding(NavigationDrawerItemDefaults.ItemPadding)
                        .padding(start = 32.dp)
                )
            }
        } else {
            NavigationDrawerItem(
                label = { Text("Làm mới tin nhắn") },
                icon = { Icon(Icons.Default.Refresh, contentDescription = null) },
                selected = selectedItem == "Làm mới tin nhắn",
                onClick = {
                    coroutineScope.launch { drawerState.close() }
                    chatViewModel.resetMessages()
                    onItemSelected("Chế độ trò chuyện")
                    Toast.makeText(context, "Tin nhắn đã được làm mới", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )
        }
    }
}