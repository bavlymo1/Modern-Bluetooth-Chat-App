package com.example.bluetooth_chat_app.chat.presentation.state

import com.example.bluetooth_chat_app.chat.domain.model.BluetoothDevice
import com.example.bluetooth_chat_app.chat.domain.model.BluetoothMessage

data class BluetoothUiState(
    val scannedDevices: List<BluetoothDevice> = emptyList(),
    val pairedDevices: List<BluetoothDevice> = emptyList(),
    val isConnected: Boolean = false,
    val isConnecting: Boolean = false,
    val errorMessage: String? = null,
    val messages: List<BluetoothMessage> = emptyList()
)
