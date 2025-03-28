package com.example.botchat.chatbox.model

sealed class UiEvent {
    data class MakePhoneCall(val number: String) : UiEvent()
    data class OpenDialer(val number: String) : UiEvent()
}