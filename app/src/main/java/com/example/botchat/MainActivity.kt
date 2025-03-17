package com.example.botchat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider
import com.example.botchat.account.viewModel.AccountViewModel
import com.example.botchat.uiMain.NavigationDrawerExample
import com.example.botchat.chatbox.viewModel.ChatViewModel
import com.example.botchat.converstation.viewModel.VoidChatViewModel
import com.example.botchat.ui.theme.BotChatTheme
import com.google.firebase.database.FirebaseDatabase

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
        val accountViewModel = ViewModelProvider(this)[AccountViewModel::class.java]
        val chatViewModel = ViewModelProvider(this)[ChatViewModel::class.java]
        val voidChatViewModel = ViewModelProvider(this)[VoidChatViewModel::class.java]
        setContent {
            BotChatTheme {
                NavigationDrawerExample(
                    chatViewModel = chatViewModel,
                    voidChatViewModel = voidChatViewModel,
                    accountViewModel=accountViewModel
                )
            }
        }
    }
}
