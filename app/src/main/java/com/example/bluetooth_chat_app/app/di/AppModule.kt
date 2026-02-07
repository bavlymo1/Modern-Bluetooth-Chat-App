package com.example.bluetooth_chat_app.app.di

import android.content.Context
import com.example.bluetooth_chat_app.chat.data.repository.AndroidBluetoothController
import com.example.bluetooth_chat_app.chat.domain.repository.BluetoothController
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    fun provideBluetoothController(@ApplicationContext context: Context): BluetoothController {
        return AndroidBluetoothController(context)
    }
}