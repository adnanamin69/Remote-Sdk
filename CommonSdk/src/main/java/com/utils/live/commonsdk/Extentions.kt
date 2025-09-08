package com.utils.live.commonsdk

import android.content.Context
import android.net.ConnectivityManager
import android.util.Log
import android.view.KeyEvent
import com.connectsdk.device.ConnectableDevice
import com.connectsdk.device.ConnectableDeviceListener
import com.connectsdk.service.DeviceService
import com.connectsdk.service.capability.CapabilityMethods
import com.connectsdk.service.capability.KeyControl
import com.connectsdk.service.capability.MediaControl
import com.connectsdk.service.capability.PlaylistControl
import com.connectsdk.service.capability.PowerControl
import com.connectsdk.service.capability.VolumeControl
import com.connectsdk.service.capability.listeners.ResponseListener
import com.connectsdk.service.command.ServiceCommandError
import com.sdk.remote.androidremote.ConnectionListener
import com.sdk.remote.androidremote.DeviceType
import com.sdk.remote.androidremote.android.PairingManager
import com.sdk.remote.androidremote.fire.FireTvConnection
import com.sdk.remote.androidremote.getFireKey
import com.sdk.remote.androidremote.hisense.MqttClientAnd
import com.sdk.remote.androidremote.hisense.toVidaaKeyCode
import com.sdk.remote.androidremote.isPortOpen
import com.sdk.remote.androidremote.isValidIP
import com.sdk.remote.androidremote.roku.RokuViewModel
import com.sdk.remote.androidremote.samsung.SamsungTVWebSocket
import com.sdk.remote.androidremote.samsung.getCommand
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

fun Context.isInternetConnection(): Boolean {
    val connectivityManager =
        getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetworkInfo = connectivityManager.activeNetworkInfo
    return activeNetworkInfo != null && activeNetworkInfo.isConnected
}


// Helper function to check and use capabilities safely
inline fun <reified T : CapabilityMethods> ConnectableDevice.useCapability(
    capabilityName: String = T::class.java.simpleName,
    action: T.() -> Unit,
) {
    val capability = getCapability(T::class.java)
    if (capability != null) {
        capability.action()
    } else {
        Log.e("TVConnection", "Device does not support $capabilityName capability")
    }
}


// Updated lgButtonPress function
fun ConnectableDevice.lgButtonPress(key: Int) {
    val buttonCallback1 = object : ResponseListener<Any> {
        override fun onError(error: ServiceCommandError?) {
            Log.e("TVConnection", "Button press error: ${error?.message ?: "Unknown error"}")
        }

        override fun onSuccess(response: Any?) {
            Log.i("TVConnection", "Button press success")
        }
    }


    val buttonCallback = object : ResponseListener<Any> {
        override fun onError(error: ServiceCommandError?) {
            Log.e("TVConnection", "Button press error: ${error?.message ?: "Unknown error"}")
        }

        override fun onSuccess(response: Any?) {
            Log.i("TVConnection", "Button press success")
        }
    }

    when (key) {
        KeyEvent.KEYCODE_DPAD_CENTER -> useCapability<KeyControl> { ok(buttonCallback) }
        KeyEvent.KEYCODE_DPAD_UP -> useCapability<KeyControl> { up(buttonCallback) }
        KeyEvent.KEYCODE_DPAD_DOWN -> useCapability<KeyControl> { down(buttonCallback) }
        KeyEvent.KEYCODE_DPAD_LEFT -> useCapability<KeyControl> { left(buttonCallback) }
        KeyEvent.KEYCODE_DPAD_RIGHT -> useCapability<KeyControl> { right(buttonCallback) }
        KeyEvent.KEYCODE_BACK -> useCapability<KeyControl> { back(buttonCallback) }
        /*      KeyEvent.KEYCODE_SETTINGS -> useCapability<KeyControl> {
                  sendKeyCode(
                      KeyControl.KeyCode.createFromInteger(KeyEvent.KEYCODE_SETTINGS),
                      buttonCallback
                  )
              }*/

        KeyEvent.KEYCODE_POWER -> useCapability<PowerControl> { powerOff(buttonCallback) }
        KeyEvent.KEYCODE_HOME -> useCapability<KeyControl> { home(buttonCallback) }
        KeyEvent.KEYCODE_MEDIA_NEXT -> useCapability<PlaylistControl> { next(buttonCallback) }
        KeyEvent.KEYCODE_MEDIA_PREVIOUS -> useCapability<PlaylistControl> { previous(buttonCallback) }
        KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> useCapability<MediaControl> {
            getPlayState(object : MediaControl.PlayStateListener {
                override fun onError(error: ServiceCommandError?) {
                    Log.e("LGTV", "PlayState error: ${error?.message ?: "Unknown error"}")
                }

                override fun onSuccess(state: MediaControl.PlayStateStatus?) {
                    when (state) {
                        MediaControl.PlayStateStatus.Playing -> pause(buttonCallback)
                        MediaControl.PlayStateStatus.Paused -> play(buttonCallback)
                        else -> play(buttonCallback)
                    }
                }
            })
        }

        KeyEvent.KEYCODE_MEDIA_FAST_FORWARD -> useCapability<MediaControl> {
            fastForward(
                buttonCallback
            )
        }

        KeyEvent.KEYCODE_MEDIA_REWIND -> useCapability<MediaControl> { rewind(buttonCallback) }
        KeyEvent.KEYCODE_VOLUME_MUTE -> useCapability<VolumeControl> {
            getMute(object : VolumeControl.MuteListener {
                override fun onError(error: ServiceCommandError?) {
                    Log.e("LGTV", "Mute listener error: ${error?.message ?: "Unknown error"}")
                }

                override fun onSuccess(isMuted: Boolean) {
                    setMute(!isMuted, buttonCallback)
                }
            })
        }

        KeyEvent.KEYCODE_VOLUME_UP -> useCapability<VolumeControl> { volumeUp(buttonCallback) }
        KeyEvent.KEYCODE_VOLUME_DOWN -> useCapability<VolumeControl> { volumeDown(buttonCallback) }
        else -> Log.w("LGTV", "Unsupported key pressed: $key")
    }
}


fun ConnectableDevice.setPinCode(string: String) {
    when (type) {
        DeviceType.FIRE -> fireTvConnection?.verifyPin(string)
        DeviceType.SAMSUNG -> TODO()
        DeviceType.LG -> sendPairingKey(string)
        DeviceType.ANDROID -> androidPairingManager.finishPairing(string)
        DeviceType.ROKU -> TODO()
        DeviceType.HISENSE -> hisenseTvConnection.sendPairingKey(string)
    }
}

suspend fun ConnectableDevice.connect(context: Context, listener: ConnectionListener) {
    withContext(Dispatchers.IO) {
        type =
            if (manufacture?.contains("Samsung", true) == true)
                DeviceType.SAMSUNG
            else if (isPortOpen(ipAddress, 6467))
                DeviceType.ANDROID
            else if (manufacture?.contains("Amazon", true) == true)
                DeviceType.FIRE
            else if (manufacture?.contains("Roku", true) == true)
                DeviceType.ROKU
            else if (manufacture?.contains("Lg", true) == true)
                DeviceType.LG
            else DeviceType.HISENSE

        if (ipAddress?.isValidIP() == false) {
            listener.onFailedToConnect("Invalid IP Address")
            return@withContext
        }
        withContext(Dispatchers.Main) {
            when (type) {
                DeviceType.SAMSUNG -> samsungTVWebSocket =
                    SamsungTVWebSocket(context, ipAddress, listener)

                DeviceType.LG -> {

                    addListener(object : ConnectableDeviceListener {
                        override fun onConnectionFailed(
                            device: ConnectableDevice?,
                            error: ServiceCommandError?
                        ) {
                            listener.onFailedToConnect(error?.message ?: "Connection failed")
                        }

                        override fun onDeviceReady(device: ConnectableDevice?) {
                            Log.d("TVConnection", "Device Connected: - ${device?.friendlyName}")
                            listener.onConnect()
                        }

                        override fun onDeviceDisconnected(device: ConnectableDevice?) {
                            listener.onDisconnect()
                        }

                        override fun onPairingRequired(
                            device: ConnectableDevice?,
                            service: DeviceService?,
                            pairingType: DeviceService.PairingType?
                        ) {
                            when (pairingType) {
                                DeviceService.PairingType.PIN_CODE -> listener.onPinRequired(8)
                                DeviceService.PairingType.FIRST_SCREEN -> listener.onPinRequired(0)
                                else -> Log.d(
                                    "TVPairing",
                                    "Pairing required. Type: ${pairingType?.name}"
                                )
                            }
                        }

                        override fun onCapabilityUpdated(
                            device: ConnectableDevice?,
                            added: MutableList<String>?,
                            removed: MutableList<String>?
                        ) {
                        }
                    })

                    Log.i("TVConnection", "connect: $isConnected")
                    if (!isConnected)
                        setPairingType(DeviceService.PairingType.PIN_CODE)
                    connect()
                }

                DeviceType.ANDROID -> androidPairingManager =
                    PairingManager(context, ipAddress, listener)

                DeviceType.ROKU -> rokuViewModel = RokuViewModel(ipAddress, listener)
                DeviceType.FIRE -> fireTvConnection = FireTvConnection(context, ipAddress, listener)
                DeviceType.HISENSE -> hisenseTvConnection =
                    MqttClientAnd(context, ipAddress, listener)
            }
        }
    }
}

fun ConnectableDevice.sendCommand(key: Int) {
    when (type) {
        DeviceType.FIRE -> fireTvConnection?.fireCommand(key.getFireKey())
        DeviceType.SAMSUNG -> samsungTVWebSocket?.sendCommand(key.getCommand())
        DeviceType.LG -> lgButtonPress(key)
        DeviceType.ANDROID -> androidPairingManager?.remote?.sendKey(key)
        DeviceType.ROKU -> rokuViewModel?.keyPress(key)
        DeviceType.HISENSE -> hisenseTvConnection?.sendKeyCode(key.toVidaaKeyCode())
    }
}

