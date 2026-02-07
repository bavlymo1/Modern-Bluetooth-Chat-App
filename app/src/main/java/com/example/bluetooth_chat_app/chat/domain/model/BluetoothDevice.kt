package com.example.bluetooth_chat_app.chat.domain.model

typealias BluetoothDeviceDomain = BluetoothDevice

data class BluetoothDevice(
    val name: String?,
    val address: String
)
