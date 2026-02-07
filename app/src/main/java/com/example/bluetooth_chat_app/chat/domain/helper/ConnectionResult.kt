package com.example.bluetooth_chat_app.chat.domain.helper

import com.example.bluetooth_chat_app.chat.domain.model.BluetoothMessage

sealed interface ConnectionResult {
    data object ServerStarted : ConnectionResult
    object ConnectionEstablished : ConnectionResult
    data class TransferSucceeded(val message: BluetoothMessage) : ConnectionResult
    data class Error(val message: String): ConnectionResult
}