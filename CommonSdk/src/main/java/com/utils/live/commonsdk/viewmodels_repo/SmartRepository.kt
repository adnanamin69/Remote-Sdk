package com.utils.live.commonsdk.viewmodels_repo

import androidx.annotation.Keep
import com.connectsdk.device.ConnectableDevice
import com.sdk.remote.androidremote.DeviceType
import com.sdk.remote.androidremote.isValidIP
import com.sdk.remote.androidremote.models.App
import com.utils.live.commonsdk.models.SavedDevice
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update


@Keep
data class SmartRepoState(
    val tvs: List<ConnectableDevice> = emptyList(),
    val connectedDevice: ConnectableDevice? = null,
    var savedDevices: List<SavedDevice> = emptyList(),
    var noInternet: Boolean = false
)

class SmartRepository(val preferencesHelper: PreferencesHelper) {
    private val _state = MutableStateFlow(SmartRepoState())
    val state: StateFlow<SmartRepoState> = _state


    fun updateState(update: (oldState: SmartRepoState) -> SmartRepoState): SmartRepoState {
        _state.update { update(it) }
        return _state.value
    }

    fun setTvs(tvs: List<ConnectableDevice>) {
        _state.update { it.copy(tvs = tvs.filter { it.ipAddress.isValidIP() }) }


        /*   _state.update {
               it.copy(tvs = tvs.filter {
                   it.manufacture?.contains("Amazon", true) == true
               })
           }*/
    }


    fun loadSavedDevices() {
        val devices = preferencesHelper.getDevices()
        updateState { it.copy(savedDevices = devices) }
    }


    fun setConnectedDevice(device: ConnectableDevice?) {
        _state.update { it.copy(connectedDevice = device) }
    }

    fun clearConnectedDevice() {
        _state.update { it.copy(connectedDevice = null) }
    }


    fun removeSavedDevice(ipAddress: String) {
        preferencesHelper.removeDevice(
            ipAddress
        )
        loadSavedDevices()
    }

    fun saveDevice(device: ConnectableDevice) {
        val savedDevice = SavedDevice(device.friendlyName, device.type.name, device.ipAddress)
        preferencesHelper.addDevice(savedDevice)
    }

}
