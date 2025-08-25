package com.adnan.remote.androidremote.roku

import android.content.Context
import android.util.Log
import android.view.KeyEvent
import android.widget.Toast
import com.adnan.remote.androidremote.ConnectionListener
import com.adnan.remote.androidremote.allowToUse
import com.wseemann.ecp.api.RokuDevice
import com.wseemann.ecp.core.KeyPressKeyValues
import com.wseemann.ecp.model.Channel
import com.wseemann.ecp.model.Device
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlin.collections.first
import kotlin.collections.forEach
import kotlin.let
import kotlin.text.lowercase
import kotlin.text.trim


private const val TAG = "RokuViewModel"

class RokuViewModel(
    private val ip: String,
    private val connectionListener: ConnectionListener
) {

    private val viewModelScope = CoroutineScope(Dispatchers.IO)
    private var device: DeviceInfo? = null

    // StateFlow to hold the list of channels
    private val _channels = MutableStateFlow<List<Channel>>(emptyList())
    val channels: StateFlow<List<Channel>> = _channels

    init {
        getDevice()
    }


    private fun fetchChannels() {
        viewModelScope.launch {
            try {
                val apps = withContext(Dispatchers.IO) {
                    device?.device?.queryAppsRequest() ?: emptyList()
                }
                _channels.value = apps
            } catch (e: Exception) {
                Log.e(TAG, "fetchChannels: ${e.message}")
            }
        }
    }

    suspend fun queryIconRequest(id: String): ByteArray? {
        return withContext(Dispatchers.IO) {
            try {
                withTimeout(1000) { // Timeout to prevent hanging
                    device?.device?.queryIconRequest(id)
                }
            } catch (e: Exception) {
                Log.i(TAG, "queryIconRequest: ${e.message}")
                null
            }
        }
    }

    fun keyPress(key1: Int) {
        val key = key1.rokuKey()
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (allowToUse())
                    device?.device?.keyPressRequest(key);
                else Log.i("SdkCheckOfUse", "roku sendKey: user not allow to use app")

            } catch (e: Exception) {
                Log.i("Exception", "keyPress: ${e.message}")
            }
        }
    }


    fun keyPress(key: String) {
        viewModelScope.launch(Dispatchers.IO) {
            // state.value.connectedDevice?.device?.keyPressRequest(key);
            //state.value.connectedDevice?.device?.keyPressRequest(" ");
            try {
                device?.device?.keyPressRequest(key);
            } catch (e: Exception) {
                Log.i("Exception", "keyPress: ${e.message}")
            }
            //state.value.connectedDevice?.device?.keyPressRequest(KeyPressKeyValues.SEARCH);
        }
    }


    fun openApp(app: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                device?.device?.queryAppsRequest()?.let { apps ->

                    apps.forEach {
                        Log.i(TAG, "openApp: ${it.title}   :  $app")
                    }
                    val channel =
                        apps.first { it.title.trim().lowercase() == app.trim().lowercase() }
                    Log.i(TAG, "openApp: ${channel.id}")

                    channel.id?.let {
                        device?.device?.launchAppIdRequest(
                            it
                        )
                    }
                }
            } catch (e: Exception) {
                Log.i(TAG, "openApp: ${e.message}")
            }
        }
    }

    private fun getDevice() {
        val rokuDevice = RokuDevice.create("http://${ip}:8060")
        viewModelScope.launch {
            try {
                device = withContext(Dispatchers.IO) {
                    DeviceInfo(rokuDevice, rokuDevice.queryDeviceInfo())
                }
                connectionListener.onConnect()
                fetchChannels()
            } catch (_: Exception) {
                connectionListener.onFailedToConnect("No device in range")
            }
        }
    }

}


data class DeviceInfo(val device: RokuDevice, val info: Device)

private fun Int.rokuKey(): KeyPressKeyValues {

    return when (this) {
        KeyEvent.KEYCODE_BACK -> KeyPressKeyValues.BACK
        KeyEvent.KEYCODE_MENU -> KeyPressKeyValues.INFO
        KeyEvent.KEYCODE_CHANNEL_UP -> KeyPressKeyValues.UP
        KeyEvent.KEYCODE_CHANNEL_DOWN -> KeyPressKeyValues.DOWN
        KeyEvent.KEYCODE_DPAD_UP -> KeyPressKeyValues.UP
        KeyEvent.KEYCODE_DPAD_DOWN -> KeyPressKeyValues.DOWN
        KeyEvent.KEYCODE_DPAD_LEFT -> KeyPressKeyValues.LEFT
        KeyEvent.KEYCODE_DPAD_RIGHT -> KeyPressKeyValues.RIGHT
        KeyEvent.KEYCODE_DPAD_CENTER -> KeyPressKeyValues.SELECT
        KeyEvent.KEYCODE_ENTER -> KeyPressKeyValues.SELECT
        KeyEvent.KEYCODE_INFO -> KeyPressKeyValues.INFO
        KeyEvent.KEYCODE_VOLUME_UP -> KeyPressKeyValues.VOLUME_UP
        KeyEvent.KEYCODE_VOLUME_DOWN -> KeyPressKeyValues.VOLUME_DOWN
        KeyEvent.KEYCODE_SETTINGS -> KeyPressKeyValues.INFO
        KeyEvent.KEYCODE_VOLUME_MUTE -> KeyPressKeyValues.VOLUME_MUTE
        KeyEvent.KEYCODE_HOME -> KeyPressKeyValues.HOME
        KeyEvent.KEYCODE_MEDIA_REWIND -> KeyPressKeyValues.LEFT
        KeyEvent.KEYCODE_MEDIA_FAST_FORWARD -> KeyPressKeyValues.RIGHT
        KeyEvent.KEYCODE_DEL -> KeyPressKeyValues.BACKSPACE
        else -> {
            KeyPressKeyValues.INPUTTUNER
        }
    }

}
