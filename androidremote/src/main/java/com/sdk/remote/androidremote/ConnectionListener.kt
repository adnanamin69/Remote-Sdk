package com.sdk.remote.androidremote

import androidx.annotation.Keep

@Keep
interface ConnectionListener {
    fun onConnect()
    fun onFailedToConnect(error: String)
    fun onPinRequired(size: Int)
    fun onDisconnect()
    fun pinError(string: String)
}

