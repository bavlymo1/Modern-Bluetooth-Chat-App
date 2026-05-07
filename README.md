# 🌸 Bluetooth Chat App (Classic RFCOMM)

![Kotlin](https://img.shields.io/badge/Kotlin-1.9.0%2B--7952FF?style=flat-square&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-1EDA8B?style=flat-square&logo=android&logoColor=white)

A Bluetooth Classic (RFCOMM) peer-to-peer chat app for Android, built with Jetpack Compose, Coroutines/Flow, and Hilt. Supports device discovery, paired device management, server/client connections, and real-time messaging over RFCOMM sockets.

> Tested conceptually for Android 11–16 using version-aware permissions and Bluetooth APIs.

---

## 📋 Screenshots

> 📍 Screenshots coming soon. Run the app locally to see it in action.

---

## ✨ Features

- Scan nearby Bluetooth Classic devices (for discovery)
- Show paired (bonded) devices
- Host a chat as a Server (listens for incoming connections)
- Join a chat as a Client (connects to a server device)
- Real-time message exchange over RFCOMM sockets
- Clean Architecture-friendly design (controller abstraction + data transfer layer)
- Version-aware Bluetooth permissions for Android 11–16

---

## 🧰 Tech Stack

| Category | Technology |
|----------|------------|
| Language | Kotlin |
| UI | Jetpack Compose |
| Concurrency | Coroutines + Flow / StateFlow |
| DI | Hilt (with KSP) |
| Bluetooth | RFCOMM - `BluetoothServerSocket`, `BluetoothSocket` |

---

## 🏗️ Architecture

```
UI (Compose) → ViewModel → BluetoothController → AndroidBluetoothController → BluetoothDataTransferService
```

- `BluetoothController` - interface (contract for discovery/connection/messaging)
- `AndroidBluetoothController` - real implementation (permissions, discovery sockets)
- `BluetoothDataTransferService` - handles InputStream/OutputStream and message streaming

---

## 🔐 Permissions

### Android 12+ (API 31+)

| Permission | Use |
|------------|-----|
| `BLUETOOTH_SCAN` | Device scan and discovery |
| `BLUETOOTH_CONNECT` | Connect, bonded devices, sockets |
| `BLUETOOTH_ADVERTISE` | Make device discoverable (optional) |

### Android 11 and below

| Permission | Use |
|------------|-----|
| `BLUETOOTH` + `BLUETOOTH_ADMIN` | Legacy access |
| `ACCESS_FINE_LOCATION` | Required for discovery on older Android |

> **Note (Android 11 and below):** Device Location toggle must be ON for discovery to return results.

---

## ▶️ How to Run

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17+
- Android SDK 31+

### Steps

1. Clone the repository
   ```bash
   git clone https://github.com/bavlymo1/Modern-Bluetooth-Chat-App.git
   cd Modern-Bluetooth-Chat-App
   ```
2. Open the project in Android Studio
3. Sync Gradle
4. Run on a physical device (Bluetooth requires real hardware)

> **Tip:** Test between two physical Android devices for the best experience. One device runs as the Server, the other as the Client.

---

## 🤝➜➢ Contributing

Contributions are welcome!

1. Fork this repository
2. Create a feature branch: `git checkout -b feature/your-feature`
3. Commit your changes: `git commit -m 'feat: add some feature'`
4. Push and open a Pull Request

---

## 📼 License

This project is open source for learning and demonstration purposes.

---

Made by [Bahy Mohy](https://github.com/bavlymo1) | [LinkedIn](https://www.linkedin.com/in/bahy-mohy-0b5ab6407/)