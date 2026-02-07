package com.example.bluetooth_chat_app.chat.data.mapper

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import com.example.bluetooth_chat_app.chat.domain.model.BluetoothDeviceDomain

@SuppressLint("MissingPermission")
fun BluetoothDevice.toBluetoothDeviceDomain(): BluetoothDeviceDomain {
    return BluetoothDeviceDomain(
        name = name,
        address = address
    )
}