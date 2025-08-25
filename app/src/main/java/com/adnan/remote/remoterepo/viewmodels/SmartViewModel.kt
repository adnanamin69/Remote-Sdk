package com.adnan.remote.remoterepo.viewmodels

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adnan.remote.androidremote.ConnectionListener
import com.adnan.remote.remoterepo.utils.connect
import com.adnan.remote.remoterepo.utils.isInternetConnection
import com.adnan.remote.remoterepo.utils.sendCommand
import com.adnan.remote.remoterepo.utils.setPinCode
import com.connectsdk.device.ConnectableDevice
import com.connectsdk.discovery.DiscoveryManager
import com.connectsdk.discovery.DiscoveryManager.PairingLevel
import com.connectsdk.discovery.DiscoveryManagerListener
import com.connectsdk.service.command.ServiceCommandError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.SocketException

private const val TAG = "SmartViewModel"

data class SmartState(
    var tvs: List<ConnectableDevice> = emptyList(),
    var connectedDevice: ConnectableDevice? = null,
)

interface Presenter {
    fun connect(device: ConnectableDevice, listener: ConnectionListener)
    fun disconnect()
    fun pinCode(string: String)
    fun sendCommand(value: Int)
}

class SmartViewModel(
    private val application: Application
) : ViewModel(), DiscoveryManagerListener, Presenter {
    private val _state =
        MutableStateFlow(SmartState())
    val state: StateFlow<SmartState> by lazy { _state }

    private var restartDiscoveryJob: Job? = null

    private val connectivityManager =
        application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            Log.i(TAG, "onAvailable: $network")
            restartDiscoveryManager() // Restart on network availability
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            Log.i(TAG, "onLost: ")
            if (!application.isInternetConnection())
                stopDiscoveryManager() // Stop on network loss
        }
    }

    private fun setupNetworkCallback() {
        val networkRequest = NetworkRequest.Builder().build()
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
    }


    private fun restartDiscoveryManager() {
        restartDiscoveryJob?.cancel() // Cancel any previous restart job
        restartDiscoveryJob = viewModelScope.launch(Dispatchers.IO) {
            stopDiscoveryManager()
            delay(2000)
            setupDiscoveryManager()
        }
    }

    init {
        DiscoveryManager.init(application)
        setupNetworkCallback()
        setupDiscoveryManager()
    }

    private fun updateState(update: (oldState: SmartState) -> SmartState): SmartState {
        _state.update { update(it) }
        return _state.value
    }

    private fun setupDiscoveryManager() {
        Log.i(TAG, "setupDiscoveryManager: ")
        DiscoveryManager.init(application)
        with(DiscoveryManager.getInstance()) {
            pairingLevel = PairingLevel.ON
            addListener(this@SmartViewModel)
            start()
        }
    }

    private fun stopDiscoveryManager() {
        Log.i(TAG, "Attempting to stop Discovery Manager")
        viewModelScope.launch {
            try {
                withContext(Dispatchers.Default) {
                    val manager = DiscoveryManager.getInstance()
                    if (manager != null) {
                        // Stop the discovery and destroy the manager
                        manager.stop()
                        DiscoveryManager.destroy()
                        Log.i(TAG, "Discovery Manager stopped successfully")
                    } else {
                        Log.w(TAG, "Discovery Manager is not initialized")
                    }
                }
            } catch (e: SocketException) {
                Log.e(TAG, "SocketException: Socket is closed while stopping Discovery Manager", e)
            } catch (e: Exception) {
                Log.e(TAG, "Error stopping Discovery Manager", e)
            }
        }
    }


    override fun onDeviceAdded(manager: DiscoveryManager?, device: ConnectableDevice?) {
        manager?.compatibleDevices?.values?.distinctBy { it.ipAddress }?.let { list ->
            updateState {
                it.copy(tvs = list)
            }
        }

    }

    override fun onDeviceUpdated(manager: DiscoveryManager?, device: ConnectableDevice?) {
        manager?.compatibleDevices?.values?.distinctBy { it.ipAddress }?.let { list ->
            updateState {
                it.copy(tvs = list)
            }
        }

    }


    override fun onDeviceRemoved(manager: DiscoveryManager?, device: ConnectableDevice?) {
        Log.i(TAG, "onDeviceRemoved: ${device?.friendlyName}")
        manager?.compatibleDevices?.values?.distinctBy { it.ipAddress }?.let { list ->
            updateState {
                it.copy(tvs = list)
            }
        }

    }

    override fun onDiscoveryFailed(manager: DiscoveryManager?, error: ServiceCommandError?) {
        Log.i(TAG, "onDiscoveryFailed: ${error?.message}")
    }

    override fun connect(
        device: ConnectableDevice,
        listener: ConnectionListener
    ) {
        viewModelScope.launch {
            updateState {
                _state.value.copy(connectedDevice = device)
            }
            _state.value.connectedDevice?.connect(application, listener)
        }

    }

    override fun disconnect() {
        updateState {
            _state.value.copy(connectedDevice = null)
        }
    }

    override fun pinCode(string: String) {
        _state.value.connectedDevice?.setPinCode(string)
    }

    override fun sendCommand(value: Int) {
        _state.value.connectedDevice?.sendCommand(value)
    }

}
