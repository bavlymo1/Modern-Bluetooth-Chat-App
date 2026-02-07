package com.example.bluetooth_chat_app.chat.domain.model

data class BluetoothMessage(
    val message: String,
    val senderName: String,
    val isFromLocalUser: Boolean
)
