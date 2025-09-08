package com.utils.live.commonsdk.viewmodels_repo

import android.app.Application
import android.util.Log
import androidx.annotation.Keep
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.connectsdk.device.ConnectableDevice
import com.connectsdk.service.capability.Launcher
import com.connectsdk.service.capability.Launcher.AppLaunchListener
import com.connectsdk.service.command.ServiceCommandError
import com.connectsdk.service.sessions.LaunchSession
import com.sdk.remote.androidremote.ConnectionListener
import com.sdk.remote.androidremote.DeviceType
import com.sdk.remote.androidremote.models.App
import com.sdk.remote.androidremote.roku.RokuViewModel
import com.sdk.remote.androidremote.samsung.samsungId
import com.utils.live.commonsdk.sendCommand
import com.wseemann.ecp.model.Channel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "SmartViewModel"

@Keep
class SmartViewModel(
    private val application: Application,
    val repo: SmartRepository,
    discoveryService: DeviceDiscoveryService
) : ViewModel(), Presenter {

    init {
        // Initialize the discovery service if not already running
        discoveryService.initialize()
        repo.loadSavedDevices()
    }


    override fun connect(
        device: ConnectableDevice,
        listener: ConnectionListener
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.setConnectedDevice(device)
            repo.state.value.connectedDevice?.connect(application, listener)
        }
    }

    private suspend fun ConnectableDevice.disconnectNow() {
        withContext(Dispatchers.IO) {

            try {

                when (type) {
                    DeviceType.ANDROID -> {
                        androidPairingManager.remote?.stop()
                    }

                    DeviceType.HISENSE -> hisenseTvConnection.disconnectAllClient()
                    else -> {}
                }
            } catch (e: Exception) {

            }
            androidPairingManager = null
            hisenseTvConnection = null
            fireTvConnection = null
            rokuViewModel = null
            samsungTVWebSocket = null
        }

    }

    override fun disconnect() {
        viewModelScope.launch {

            repo.state.value.connectedDevice?.disconnectNow()
            delay(1000)
            repo.clearConnectedDevice()

            // Stop foreground service when disconnecting
            //  RemoteForegroundService.stopService(application)
        }
    }

    override fun pinCode(string: String) {
        repo.state.value.connectedDevice?.setPinCode(string)
    }

    override fun sendCommand(value: Int) {
        repo.state.value.connectedDevice?.sendCommand(value)
    }

    override fun onAppPress(app: App) {
        val state = repo.state
        when (state.value.connectedDevice?.type) {
            DeviceType.SAMSUNG -> {
                // state.value.co nnectedDevice?.samsungTVWebSocket.requestInstalledApps()
                viewModelScope.launch(Dispatchers.IO) {
                    state.value.connectedDevice?.samsungTVWebSocket?.launchAppViaHttp(app.name.samsungId())
                }
            }

            DeviceType.ANDROID -> state.value.connectedDevice?.androidPairingManager?.remote?.launchApp(
                app.content
            )

            DeviceType.HISENSE -> state.value.connectedDevice?.hisenseTvConnection?.launchApp(
                app
            )

            DeviceType.FIRE -> state.value.connectedDevice?.fireTvConnection?.launchApps(app.activityName)

            DeviceType.LG -> state.value.connectedDevice?.openLgApp(app.content)
            DeviceType.ROKU -> state.value.connectedDevice?.launchRokuApp(app)
            null -> {}
        }

    }

    override fun getApps(default: List<App>): List<App>? {
        return when (repo.state.value.connectedDevice?.type) {
            DeviceType.ANDROID, DeviceType.SAMSUNG -> default
            DeviceType.LG -> default.filter { app -> repo.state.value.connectedDevice?.lgApps?.any { it.name == app.name } == true }
            DeviceType.ROKU -> repo.state.value.connectedDevice?.rokuViewModel?.channels?.value?.map {
                val icon = repo.state.value.connectedDevice?.rokuViewModel?.getIconURL(it.id) ?: ""
                App(0, name = it.title, it.id, url = icon)
            }

            DeviceType.FIRE -> repo.state.value.connectedDevice?.fireTvConnection?.channelsList
            DeviceType.HISENSE -> repo.state.value.connectedDevice?.hisenseTvConnection?.listOfApps
            null -> null
        }
    }

    override fun onSendText(text: String) {
        //  TODO("Not yet implemented")
    }


    override fun removeSavedDevice(ipAddress: String) {
        repo.removeSavedDevice(ipAddress)
    }

    override fun sendVoice(value: Int) {
        //TODO("Not yet implemented")
    }

    override fun senVoicePayload(bytes: ByteArray) {
        // TODO("Not yet implemented")
    }

    override fun saveDevice(device: ConnectableDevice) {
        repo.saveDevice(device)
    }

}

private fun ConnectableDevice.launchRokuApp(app: App) {
    rokuViewModel.openApp(app.content)
}

private fun ConnectableDevice.openLgApp(content: String) {
    getCapability(Launcher::class.java)
        ?.launchApp(
            content,
            object : AppLaunchListener {
                override fun onError(error: ServiceCommandError) {
                    Log.d(
                        TAG,
                        "App Launch error: ${error.message}"
                    )
                }

                override fun onSuccess(`object`: LaunchSession) {
                    Log.d(
                        TAG,
                        "App Launch success."
                    )
                }
            })
}


private fun RokuViewModel.getIconURL(channelId: String): String {
    var url = ""

    try {
        url = "${getRoku()?.host}/query/icon/$channelId"
    } catch (ex: Exception) {
        // Handle the exception if needed
    }

    return url
}



