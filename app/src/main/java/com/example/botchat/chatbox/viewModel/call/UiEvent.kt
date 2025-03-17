package com.example.botchat.chatbox.viewModel.call

sealed class UiEvent {
    data class MakePhoneCall(val number: String) : UiEvent()
    data class OpenDialer(val number: String) : UiEvent()
}