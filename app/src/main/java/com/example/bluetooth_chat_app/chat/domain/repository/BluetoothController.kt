package com.example.bluetooth_chat_app.chat.domain.repository

import com.example.bluetooth_chat_app.chat.domain.helper.ConnectionResult
import com.example.bluetooth_chat_app.chat.domain.model.BluetoothDevice
import com.example.bluetooth_chat_app.chat.domain.model.BluetoothMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface BluetoothController {
    val isConnected: StateFlow<Boolean>
    val scannedDevices: StateFlow<List<BluetoothDevice>>
    val pairedDevices: StateFlow<List<BluetoothDevice>>
    val errors: SharedFlow<String>

    fun startDiscovery()
    fun stopDiscovery()

    fun startBluetoothServer() : Flow<ConnectionResult>
    fun connectToDevice(device: BluetoothDevice): Flow<ConnectionResult>
    suspend fun trySendMessage(message: String): BluetoothMessage?
    fun closeConnection()

    fun release()
}