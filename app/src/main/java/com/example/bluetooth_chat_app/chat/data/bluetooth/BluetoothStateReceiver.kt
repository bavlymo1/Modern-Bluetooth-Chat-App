package com.example.bluetooth_chat_app.chat.data.bluetooth

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

class BluetoothStateReceiver(
    private val onStateChanged: (isConnected: Boolean, device: BluetoothDevice) -> Unit
) : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val device = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent?.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent?.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
        } ?: return

        when (intent?.action) {
            BluetoothDevice.ACTION_ACL_CONNECTED -> onStateChanged(true, device)
            BluetoothDevice.ACTION_ACL_DISCONNECTED -> onStateChanged(false, device)
        }
    }
}