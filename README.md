# Bluetooth Chat App (Classic RFCOMM)

A Bluetooth Classic (RFCOMM) peer-to-peer chat app for Android, built with **Jetpack Compose**, **Coroutines/Flow**, and **Hilt**.  
It supports **device discovery**, **paired devices**, **server/client connections**, and **real-time messaging**.

> Tested conceptually for Android 11–16 by using version-aware permissions and Bluetooth APIs.

---

## ✨ Features

- 🔎 Scan nearby Bluetooth Classic devices (discovery)
- 🔗 Show paired (bonded) devices
- 🖥️ Host chat as a **Server** (listens for incoming connections)
- 📱 Join chat as a **Client** (connects to a server device)
- 💬 Real-time message exchange over RFCOMM sockets
- 🧠 Clean Architecture-friendly design (controller abstraction + data transfer layer)
- ✅ Android 12+ runtime Bluetooth permissions + legacy support for Android 11 and below

---

## 🧰 Tech Stack

- Kotlin
- Jetpack Compose
- Coroutines + Flow / StateFlow
- Hilt (DI) with KSP
- Bluetooth Classic APIs (RFCOMM): `BluetoothServerSocket`, `BluetoothSocket`

---

## 🏗️ Architecture (High Level)

**UI (Compose) → ViewModel → BluetoothController → AndroidBluetoothController → BluetoothDataTransferService**

- `BluetoothController`: interface (contract for discovery/connection/messaging)
- `AndroidBluetoothController`: real implementation (permissions, discovery, sockets)
- `BluetoothDataTransferService`: handles InputStream/OutputStream and message streaming

---

## 🔐 Permissions

### Android 12+ (API 31+)
- `BLUETOOTH_SCAN` (scan/discovery)
- `BLUETOOTH_CONNECT` (connect, bonded devices, sockets)
- `BLUETOOTH_ADVERTISE` *(only if you make device discoverable via app)*

### Android 11 and below
- `BLUETOOTH` and `BLUETOOTH_ADMIN` (legacy)
- `ACCESS_FINE_LOCATION` (required for discovery on older Android versions)

⚠️ **Android 11 and below:** Device **Location toggle must be ON** for discovery to return results.

---

## ▶️ How to Run

1. Clone the repository:
   ```bash
   https://github.com/bavlymo1/Modern-Bluetooth-Chat-App.git

