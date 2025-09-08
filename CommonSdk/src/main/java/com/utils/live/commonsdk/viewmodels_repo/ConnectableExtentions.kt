package com.utils.live.commonsdk.viewmodels_repo

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.connectsdk.core.AppInfo
import com.sdk.remote.androidremote.ConnectionListener
import com.sdk.remote.androidremote.DeviceType
import com.sdk.remote.androidremote.fire.FireTvConnection
import com.sdk.remote.androidremote.getFireKey
import com.sdk.remote.androidremote.isPortOpen
import com.sdk.remote.androidremote.isValidIP
import com.connectsdk.device.ConnectableDevice
import com.connectsdk.device.ConnectableDeviceListener
import com.connectsdk.service.DeviceService
import com.connectsdk.service.capability.Launcher
import com.connectsdk.service.command.ServiceCommandError
import com.sdk.remote.androidremote.android.PairingManager
import com.sdk.remote.androidremote.hisense.MqttClientAnd
import com.sdk.remote.androidremote.models.App
import com.sdk.remote.androidremote.roku.RokuViewModel
import com.sdk.remote.androidremote.samsung.SamsungTVWebSocket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


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

                            device?.launcher?.getAppList(object : Launcher.AppListListener {
                                override fun onError(error: ServiceCommandError?) {
                                    Log.i("", "Apps error $error")
                                }

                                override fun onSuccess(apps: MutableList<AppInfo>?) {
                                    lgApps = apps?.map {
                                        App(0, it.name, it.id)
                                    }
                                }
                            })

                            lgApps = device?.getLgApps()

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

fun ConnectableDevice.sendCommand(key: Int, context: Context) {
    when (type) {
        DeviceType.FIRE -> fireTvConnection?.fireCommand(key.getFireKey())
        else -> Toast.makeText(context, "Open Ad", Toast.LENGTH_SHORT).show()
    }
}
