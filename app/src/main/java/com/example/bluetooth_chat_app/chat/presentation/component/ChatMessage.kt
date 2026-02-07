package com.example.bluetooth_chat_app.chat.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bluetooth_chat_app.chat.domain.model.BluetoothMessage
import com.example.bluetooth_chat_app.ui.theme.Bluetooth_Chat_AppTheme
import com.example.bluetooth_chat_app.ui.theme.OldRose
import com.example.bluetooth_chat_app.ui.theme.Vanilla

@Composable
fun ChatMessage(
    message: BluetoothMessage,
    modifier: Modifier = Modifier
){
    Column(
        modifier = modifier
            .clip(
                RoundedCornerShape(
                    topStart = if (message.isFromLocalUser) 15.dp else 0.dp,
                    topEnd = 15.dp,
                    bottomStart = 15.dp,
                    bottomEnd = if (message.isFromLocalUser) 0.dp else 15.dp,
                )
            ).background(
                if (message.isFromLocalUser) OldRose else Vanilla
            ).padding(16.dp)
    ) {
        Text(
            text = message.senderName,
            color = Color.Black,
            fontSize = 10.sp
        )
        Text(
            text = message.message,
            color = Color.Black,
            modifier = Modifier.widthIn(max = 250.dp)
        )
    }
}

@Preview
@Composable
fun ChatMessagePreview() {
    Bluetooth_Chat_AppTheme {
        ChatMessage(
            message = BluetoothMessage(
                message = "Hello World!",
                senderName = "Pixel 6",
                isFromLocalUser = false
            )
        )
    }
}