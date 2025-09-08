package com.utils.live.commonsdk.viewmodels_repo

import com.connectsdk.device.ConnectableDevice
import com.sdk.remote.androidremote.ConnectionListener
import com.sdk.remote.androidremote.models.App

interface Presenter {
    fun connect(device: ConnectableDevice, listener: ConnectionListener)
    fun disconnect()
    fun pinCode(string: String)
    fun sendCommand(value: Int)
    fun onAppPress(app: App)

    fun getApps(default: List<App>): List<App>?

    fun onSendText(text: String)
    fun removeSavedDevice(ipAddress: String)
    fun sendVoice(value: Int)
    fun senVoicePayload(bytes: ByteArray)
    fun saveDevice(device: ConnectableDevice)
}
