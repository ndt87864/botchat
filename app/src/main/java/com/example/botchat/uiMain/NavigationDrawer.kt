package com.example.botchat.uiMain

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.botchat.account.ui.edit.EditAccountPage
import com.example.botchat.account.ui.authentication.forgot.ForgotPasswordPage
import com.example.botchat.account.ui.authentication.login.LoginRegisterPage
import com.example.botchat.account.viewModel.AccountViewModel
import com.example.botchat.chatbox.ui.history.ChatHistoryPage
import com.example.botchat.chatbox.ui.chat.ChatPage
import com.example.botchat.chatbox.viewModel.ChatViewModel
import com.example.botchat.chatbox.ui.setup.SettingChatBot
import com.example.botchat.converstation.ui.setting.VoidSettingsPage
import com.example.botchat.converstation.ui.dialog.VoidChatPage
import com.example.botchat.converstation.viewModel.VoidChatViewModel
import com.example.botchat.converstation.ui.historySetting.VoidSettingsHistoryPage
import com.example.botchat.extends.ExtendsPage
import com.example.botchat.uiMain.components.DrawerContent
import com.example.botchat.uiMain.components.TopBar
import kotlinx.coroutines.launch

@Composable
fun NavigationDrawerExample(
    chatViewModel: ChatViewModel,
    voidChatViewModel: VoidChatViewModel,
    accountViewModel: AccountViewModel,
    activity: Activity = (LocalContext.current as? Activity) ?: throw IllegalStateException("No Activity found")
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    var selectedItem by remember { mutableStateOf("Chế độ trò chuyện") }
    var showExitDialog by remember { mutableStateOf(false) }
    var isExitSummaryShown by remember { mutableStateOf(false) }

    // Theo dõi trạng thái đăng nhập của người dùng
    LaunchedEffect(accountViewModel.isUserLoggedIn()) {
        if (accountViewModel.isUserLoggedIn()) {
            accountViewModel.startUserListener()
        } else {
            accountViewModel.stopUserListener()
        }
    }

    // Xử lý nút quay lại
    BackHandler {
        when (selectedItem) {
            "Chế độ trò chuyện" -> showExitDialog = true
            "Tùy chỉnh" -> selectedItem = "Chế độ thoại"
            "Thêm thiết lập" -> selectedItem = "Tùy chỉnh"
            "Quên mật khẩu" -> selectedItem = "Đăng nhập/Đăng ký"
            "Đặt lại mật khẩu" -> selectedItem = "Quên mật khẩu"
            "Chế độ thoại" -> {
                if (isExitSummaryShown) {
                    voidChatViewModel.reset()
                    voidChatViewModel.conversation.clear()
                }
                selectedItem = "Chế độ trò chuyện"
            }
            else -> selectedItem = "Chế độ trò chuyện"
        }
    }

    // Hiển thị dialog thoát ứng dụng
    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("Xác nhận thoát") },
            text = { Text("Bạn có chắc chắn muốn thoát ứng dụng không?") },
            confirmButton = {
                TextButton(onClick = { activity.finish() }) { Text("Thoát") }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) { Text("Hủy") }
            }
        )
    }

    // Thanh điều hướng bên
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(
                drawerState = drawerState,
                selectedItem = selectedItem,
                onItemSelected = { selectedItem = it },
                chatViewModel = chatViewModel,
                accountViewModel = accountViewModel,
                coroutineScope = coroutineScope
            )
        }
    ) {
        Scaffold(
            modifier = Modifier.background(MaterialTheme.colorScheme.background),
            topBar = {
                TopBar(
                    selectedItem = selectedItem,
                    onItemSelected = { selectedItem = it },
                    chatViewModel = chatViewModel,
                    voidChatViewModel = voidChatViewModel,
                    accountViewModel = accountViewModel,
                    drawerState = drawerState,
                    coroutineScope = coroutineScope,
                    isExitSummaryShown = isExitSummaryShown,
                    onExitSummary = {
                        voidChatViewModel.reset()
                        voidChatViewModel.conversation.clear()
                        selectedItem = "Chế độ trò chuyện"
                    }
                )
            },
            content = { innerPadding ->
                when (selectedItem) {
                    "Chế độ trò chuyện" -> ChatPage(
                        modifier = Modifier.fillMaxSize().padding(innerPadding).background(MaterialTheme.colorScheme.background),
                        viewModel = chatViewModel,
                        roomId = chatViewModel.roomId
                    )
                    "Tiện ích" -> ExtendsPage(
                        modifier = Modifier.fillMaxSize().padding(innerPadding).background(MaterialTheme.colorScheme.background),
                        viewModel = chatViewModel
                    )
                    "Đăng nhập/Đăng ký" -> LoginRegisterPage(
                        viewModel = accountViewModel,
                        modifier = Modifier.fillMaxSize().padding(innerPadding).background(MaterialTheme.colorScheme.background),
                        onNavigateToChat = { selectedItem = "Chế độ trò chuyện" },
                        onNavigateToForgotPassword = { selectedItem = "Quên mật khẩu" }
                    )
                    "Đăng xuất" -> LoginRegisterPage(
                        viewModel = accountViewModel,
                        modifier = Modifier.fillMaxSize().padding(innerPadding).background(MaterialTheme.colorScheme.background),
                        onNavigateToChat = { selectedItem = "Chế độ trò chuyện" },
                        onNavigateToForgotPassword = { selectedItem = "Quên mật khẩu" }
                    )
                    "Quên mật khẩu" -> ForgotPasswordPage(
                        viewModel = accountViewModel,
                        modifier = Modifier.fillMaxSize().padding(innerPadding).background(MaterialTheme.colorScheme.background),
                        onNavigateToResetPassword = { selectedItem = "Đăng nhập/Đăng ký" }
                    )
                    "Lịch sử" -> ChatHistoryPage(
                        viewModel = chatViewModel,
                        modifier = Modifier.fillMaxSize().padding(innerPadding).background(MaterialTheme.colorScheme.background),
                        onChatSelected = { roomId ->
                            selectedItem = "Chế độ trò chuyện"
                            coroutineScope.launch { drawerState.close() }
                            chatViewModel.roomId = roomId
                        }
                    )
                    "Cá nhân hóa" -> SettingChatBot(
                        modifier = Modifier.fillMaxSize().padding(innerPadding),
                        chatViewModel = chatViewModel,
                        onSaveSuccess = { selectedItem = "Chế độ trò chuyện" }
                    )
                    "Tài khoản" -> EditAccountPage(
                        viewModel = accountViewModel,
                        modifier = Modifier.fillMaxSize().padding(innerPadding).background(MaterialTheme.colorScheme.background),
                        onNavigateToChat = { selectedItem = "Chế độ trò chuyện" }
                    )
                    "Chế độ thoại" -> VoidChatPage(
                        viewModel = voidChatViewModel,
                        chatViewModel = chatViewModel,
                        modifier = Modifier.fillMaxSize().padding(innerPadding).background(MaterialTheme.colorScheme.background),
                        onExitSummaryShown = { isExitSummaryShown = it },
                        onExitRequested = { selectedItem = "Chế độ trò chuyện" }
                    )
                    "Tùy chỉnh" -> VoidSettingsHistoryPage(
                        viewModel = voidChatViewModel,
                        modifier = Modifier.fillMaxSize().padding(innerPadding).background(MaterialTheme.colorScheme.background),
                        onSettingSelected = { settingId ->
                            voidChatViewModel.currentEditingSettingId.value = settingId
                            selectedItem = "Thêm thiết lập"
                        },
                        onNavigateToVoiceChat = { selectedItem = "Chế độ thoại" }
                    )
                    "Thêm thiết lập" -> VoidSettingsPage(
                        modifier = Modifier.fillMaxSize().padding(innerPadding).background(MaterialTheme.colorScheme.background),
                        voidChatViewModel = voidChatViewModel,
                        onNavigateToVoiceChat = { selectedItem = "Tùy chỉnh" }
                    )
                }
            }
        )
    }
}