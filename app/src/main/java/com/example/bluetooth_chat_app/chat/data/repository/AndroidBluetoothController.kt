package com.example.bluetooth_chat_app.chat.data.repository

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.example.bluetooth_chat_app.chat.data.bluetooth.BluetoothDataTransferService
import com.example.bluetooth_chat_app.chat.data.bluetooth.BluetoothStateReceiver
import com.example.bluetooth_chat_app.chat.data.bluetooth.FoundDeviceReceiver
import com.example.bluetooth_chat_app.chat.data.mapper.toBluetoothDeviceDomain
import com.example.bluetooth_chat_app.chat.data.mapper.toByteArray
import com.example.bluetooth_chat_app.chat.domain.helper.ConnectionResult
import com.example.bluetooth_chat_app.chat.domain.model.BluetoothDeviceDomain
import com.example.bluetooth_chat_app.chat.domain.model.BluetoothMessage
import com.example.bluetooth_chat_app.chat.domain.repository.BluetoothController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.update
import java.io.IOException
import java.util.UUID

@SuppressLint("MissingPermission")
class AndroidBluetoothController(
    private val context: Context
) : BluetoothController {

    // Access to system Bluetooth services (adapter, connection state, etc.).
    private val bluetoothManager by lazy {
        context.getSystemService(BluetoothManager::class.java)
    }

    // The device Bluetooth radio; null means Bluetooth not supported.
    private val bluetoothAdapter by lazy {
        bluetoothManager?.adapter
    }

    private var dataTransferService: BluetoothDataTransferService? = null

    // True when a bonded device is connected (based on ACL broadcasts).
    private val _isConnected = MutableStateFlow(false)
    override val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    // Devices found during discovery (scan).
    private val _scannedDevices = MutableStateFlow<List<BluetoothDeviceDomain>>(emptyList())
    override val scannedDevices: StateFlow<List<BluetoothDeviceDomain>> = _scannedDevices.asStateFlow()

    // Devices already paired (bonded) with this phone.
    private val _pairedDevices = MutableStateFlow<List<BluetoothDeviceDomain>>(emptyList())
    override val pairedDevices: StateFlow<List<BluetoothDeviceDomain>> = _pairedDevices.asStateFlow()

    // One-shot error messages for UI (snackbar/toast). Buffered to avoid suspending.
    private val _errors = MutableSharedFlow<String>(extraBufferCapacity = 1)
    override val errors: SharedFlow<String> = _errors.asSharedFlow()

    // Tracks if the ACTION_FOUND receiver is registered.
    private var foundReceiverRegistered = false

    // Tracks if the Bluetooth state receiver is registered.
    private var stateReceiverRegistered = false

    // Server socket used when hosting (listening for incoming connection).
    private var currentServerSocket: BluetoothServerSocket? = null

    // Client socket used when connecting or after accepting a connection.
    private var currentClientSocket: BluetoothSocket? = null

    // Updates scanned devices when system reports a found device.
    private val foundDeviceReceiver = FoundDeviceReceiver { device ->
        _scannedDevices.update { devices ->
            val newDevice = device.toBluetoothDeviceDomain()
            if (newDevice in devices) devices else devices + newDevice
        }
    }

    // Updates isConnected ONLY for bonded devices (avoid spamming errors for unrelated ACL events).
    private val bluetoothStateReceiver = BluetoothStateReceiver { connected, bluetoothDevice ->
        val isBonded = bluetoothAdapter
            ?.bondedDevices
            ?.any { it.address == bluetoothDevice.address } == true

        if (isBonded) {
            _isConnected.value = connected
        }
    }

    init {
        loadPairedDevices()

        // Listen to Bluetooth connection/disconnection events.
        context.registerReceiver(
            bluetoothStateReceiver,
            IntentFilter().apply {
                addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
                addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
                addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)
            }
        )
        stateReceiverRegistered = true
    }

    // Starts discovery and emits devices via scannedDevices.
    override fun startDiscovery() {
        val adapter = bluetoothAdapter ?: return
        if (!adapter.isEnabled) {
            _errors.tryEmit("Bluetooth is disabled.")
            return
        }
        if (!canScan()) {
            _errors.tryEmit("Missing scan permission.")
            return
        }

        _scannedDevices.value = emptyList()

        if (!foundReceiverRegistered) {
            context.registerReceiver(foundDeviceReceiver, IntentFilter(BluetoothDevice.ACTION_FOUND))
            foundReceiverRegistered = true
        }

        if (adapter.isDiscovering) adapter.cancelDiscovery()

        loadPairedDevices()

        val started = adapter.startDiscovery()
        if (!started) {
            _errors.tryEmit("Failed to start discovery.")
        }
    }

    override fun stopDiscovery() {
        if (!canScan()) return
        bluetoothAdapter?.cancelDiscovery()
    }

    // Starts server mode and waits for a client to connect (accept once).
    override fun startBluetoothServer(): Flow<ConnectionResult> = flow {
        if (!canConnect()) {
            emit(ConnectionResult.Error("Missing Bluetooth connect permission for this Android version"))
            return@flow
        }

        val adapter = bluetoothAdapter ?: run {
            emit(ConnectionResult.Error("Bluetooth not supported"))
            return@flow
        }

        // Discovery is heavyweight; avoid it while accepting/connecting. [5](https://stackoverflow.com/questions/79453253/kotlin-flow-onstart-wont-call-why)
        if (adapter.isDiscovering) adapter.cancelDiscovery()

        currentServerSocket = adapter.listenUsingRfcommWithServiceRecord(
            "chat_service",
            UUID.fromString(SERVICE_UUID)
        )

        // ✅ Tell UI: server is listening (accept() will block). [2](https://www.w3tutorials.net/blog/when-is-bluetooth-admin-android-permission-required/)[3](https://stuff.mit.edu/afs/sipb/project/android/docs/guide/topics/connectivity/bluetooth.html)
        emit(ConnectionResult.ServerStarted)

        val socket: BluetoothSocket = try {
            currentServerSocket?.accept()
                ?: run {
                    emit(ConnectionResult.Error("Server socket is null"))
                    return@flow
                }
        } catch (e: IOException) {
            emit(ConnectionResult.Error("Failed to accept connection: ${e.message ?: "IOException"}"))
            return@flow
        }

        currentClientSocket = socket
        runCatching { currentServerSocket?.close() }
        currentServerSocket = null

        val service = BluetoothDataTransferService(socket)
        dataTransferService = service

        emit(ConnectionResult.ConnectionEstablished)

        emitAll(
            service.listenForIncomingMessages()
                .map { msg -> ConnectionResult.TransferSucceeded(msg) }
        )
    }.flowOn(Dispatchers.IO)
        .onCompletion { closeConnection() }

    // Connects to a remote device as a client.
    override fun connectToDevice(device: BluetoothDeviceDomain): Flow<ConnectionResult> = flow {
        if (!canConnect()) {
            emit(ConnectionResult.Error("Missing Bluetooth connect permission for this Android version"))
            return@flow
        }

        val adapter = bluetoothAdapter ?: run {
            emit(ConnectionResult.Error("Bluetooth not supported"))
            return@flow
        }

        if (adapter.isDiscovering) adapter.cancelDiscovery() // recommended

        val socket = try {
            adapter
                .getRemoteDevice(device.address)
                .createRfcommSocketToServiceRecord(UUID.fromString(SERVICE_UUID))
        } catch (e: IllegalArgumentException) {
            emit(ConnectionResult.Error("Invalid device address: ${e.message}"))
            return@flow
        }

        currentClientSocket = socket

        try {
            socket.connect()
            emit(ConnectionResult.ConnectionEstablished)

            val service = BluetoothDataTransferService(socket)
            dataTransferService = service

            emitAll(
                service.listenForIncomingMessages()
                    .map { msg -> ConnectionResult.TransferSucceeded(msg) }
            )
        } catch (e: IOException) {
            runCatching { socket.close() }
            currentClientSocket = null
            emit(ConnectionResult.Error("Connect failed: ${e.message ?: "IOException"}"))
        }
    }.flowOn(Dispatchers.IO)
        .onCompletion { closeConnection() }

    override suspend fun trySendMessage(message: String): BluetoothMessage? {
        if (!canConnect()) return null
        val service = dataTransferService ?: return null

        val bluetoothMessage = BluetoothMessage(
            message = message,
            senderName = bluetoothAdapter?.name ?: "Unknown name",
            isFromLocalUser = true
        )

        service.sendMessage(bluetoothMessage.toByteArray())
        return bluetoothMessage
    }

    // Closes any open sockets and resets connection references.
    override fun closeConnection() {
        runCatching { currentClientSocket?.close() }
        runCatching { currentServerSocket?.close() }
        currentClientSocket = null
        currentServerSocket = null
        _isConnected.value = false
    }

    // Cleans up receivers + discovery + sockets to prevent leaks.
    override fun release() {
        stopDiscovery()

        if (foundReceiverRegistered) {
            runCatching { context.unregisterReceiver(foundDeviceReceiver) }
            foundReceiverRegistered = false
        }

        if (stateReceiverRegistered) {
            runCatching { context.unregisterReceiver(bluetoothStateReceiver) }
            stateReceiverRegistered = false
        }

        closeConnection()
    }

    // Updates pairedDevices from adapter bonded devices list.
    private fun loadPairedDevices() {
        if (!canReadBonded()) {
            _pairedDevices.value = emptyList()
            return
        }

        val devices = bluetoothAdapter
            ?.bondedDevices
            ?.map { it.toBluetoothDeviceDomain() }
            ?: emptyList()

        _pairedDevices.value = devices
    }

    private fun canScan(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            hasPermission(Manifest.permission.BLUETOOTH_SCAN)
        } else {
            hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun canReadBonded(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            hasPermission(Manifest.permission.BLUETOOTH_CONNECT)
        } else true
    }

    // Checks if a runtime permission is granted.
    private fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }

    private fun canConnect(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            hasPermission(Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            // On Android 11 and below, BLUETOOTH is the legacy permission used for connections.
            hasPermission(Manifest.permission.BLUETOOTH)
        }
    }

    companion object {
        const val SERVICE_UUID = "490DB153-19E5-11ED-8C90-E4A8DFE8B254"
    }
}
