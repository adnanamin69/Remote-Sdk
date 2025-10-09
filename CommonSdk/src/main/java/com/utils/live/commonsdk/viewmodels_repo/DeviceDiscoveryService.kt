package com.utils.live.commonsdk.viewmodels_repo

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiManager
import android.util.Log
import com.connectsdk.device.ConnectableDevice
import com.connectsdk.discovery.DiscoveryManager
import com.connectsdk.discovery.DiscoveryManagerListener
import com.connectsdk.service.command.ServiceCommandError
import com.sdk.remote.androidremote.DeviceType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.SocketException

private const val TAG = "DeviceDiscoveryService"

class DeviceDiscoveryService(
    private val application: Application,
    private val repository: SmartRepository
) : DiscoveryManagerListener {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var restartDiscoveryJob: Job? = null
    private var isInitialized = false

    private val connectivityManager =
        application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            Log.i(TAG, "Network available: $network")
            restartDiscoveryManager()
            repository.updateState {
                it.copy(noInternet = false)
            }
        }


        fun isWifiEnabled(): Boolean {
            val wifiManager = application.getSystemService(Context.WIFI_SERVICE) as WifiManager
            return wifiManager.isWifiEnabled
        }


        override fun onLost(network: Network) {
            super.onLost(network)
            Log.i(TAG, "Network lost")


            val cm =
                application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork = cm.activeNetwork
            val capabilities = cm.getNetworkCapabilities(activeNetwork)

            val isWifi = capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true

            Log.i(TAG, "onLost: $isWifi")

            repository.updateState {
                it.copy(noInternet = !isWifiEnabled())
            }

            if (!application.isInternetConnection()) {
                stopDiscoveryManager()
            }
        }
    }

    fun initialize() {
        if (isInitialized) return

        Log.i(TAG, "Initializing DeviceDiscoveryService")
        DiscoveryManager.init(application)
        setupNetworkCallback()
        setupDiscoveryManager()
        isInitialized = true
    }

    fun startDiscovery() {
        if (!isInitialized) {
            initialize()
        } else {
            setupDiscoveryManager()
        }
    }

    fun stopDiscovery() {
        stopDiscoveryManager()
    }

    fun shutdown() {
        if (!isInitialized) return

        Log.i(TAG, "Shutting down DeviceDiscoveryService")
        stopDiscoveryManager()
        cleanupNetworkCallback()
        isInitialized = false
    }

    private fun setupNetworkCallback() {
        val networkRequest = NetworkRequest.Builder().build()
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
    }

    private fun cleanupNetworkCallback() {
        try {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        } catch (e: Exception) {
            Log.e(TAG, "Error unregistering network callback", e)
        }
    }

    private fun restartDiscoveryManager() {
        restartDiscoveryJob?.cancel()
        restartDiscoveryJob = serviceScope.launch {
            stopDiscoveryManager()
            delay(2000)
            setupDiscoveryManager()
        }
    }

    private fun setupDiscoveryManager() {
        Log.i(TAG, "Setting up Discovery Manager")
        try {
            DiscoveryManager.init(application)
            with(DiscoveryManager.getInstance()) {
                pairingLevel = DiscoveryManager.PairingLevel.ON
                addListener(this@DeviceDiscoveryService)
                start()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up Discovery Manager", e)
        }
    }

    private fun stopDiscoveryManager() {
        Log.i(TAG, "Stopping Discovery Manager")
        serviceScope.launch {
            try {
                withContext(Dispatchers.Default) {
                    DiscoveryManager.getInstance()?.let { manager ->
                        manager.stop()
                        DiscoveryManager.destroy()
                        Log.i(TAG, "Discovery Manager stopped successfully")
                    }
                }
            } catch (e: SocketException) {
                Log.e(TAG, "SocketException while stopping Discovery Manager", e)
            } catch (e: Exception) {
                Log.e(TAG, "Error stopping Discovery Manager", e)
            }
        }
    }

    // DiscoveryManagerListener implementations
    override fun onDeviceAdded(manager: DiscoveryManager?, device: ConnectableDevice?) {
        Log.i(TAG, "Device added: ${device?.friendlyName} (${device?.ipAddress})")
        manager?.compatibleDevices?.values?.distinctBy { it.ipAddress }?.let { list ->
            Log.i(TAG, "Total devices discovered: ${list.size}")
            repository.setTvs(list)
        }
    }

    override fun onDeviceUpdated(manager: DiscoveryManager?, device: ConnectableDevice?) {
        Log.i(TAG, "Device updated: ${device?.friendlyName} (${device?.ipAddress})")
        manager?.compatibleDevices?.values?.distinctBy { it.ipAddress }?.let { list ->
            Log.i(TAG, "Total devices discovered: ${list.size}")
            repository.setTvs(list)
        }
    }

    override fun onDeviceRemoved(manager: DiscoveryManager?, device: ConnectableDevice?) {
        Log.i(TAG, "Device removed: ${device?.friendlyName} (${device?.ipAddress})")
        manager?.compatibleDevices?.values?.distinctBy { it.ipAddress }?.let { list ->
            Log.i(TAG, "Total devices discovered: ${list.size}")
            repository.setTvs(list)
        }
    }

    override fun onDiscoveryFailed(manager: DiscoveryManager?, error: ServiceCommandError?) {
        Log.e(TAG, "Discovery failed: ${error?.message}")
    }
}
