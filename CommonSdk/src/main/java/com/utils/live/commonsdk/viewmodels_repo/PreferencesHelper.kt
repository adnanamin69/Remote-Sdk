package com.utils.live.commonsdk.viewmodels_repo

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.utils.live.commonsdk.models.SavedDevice
import kotlinx.serialization.json.Json
import kotlin.collections.indexOfFirst
import kotlin.collections.removeAll
import kotlin.collections.toMutableList

private const val SAVED_DEVICES_KEY = "saved_devices"


class PreferencesHelper(context: Context) {
    val name: String = "app_preferences"
    private val prefs: SharedPreferences =
        context.getSharedPreferences(name, Context.MODE_PRIVATE)

    private val json = Json { ignoreUnknownKeys = true }

    fun putString(key: String, value: String) {
        prefs.edit { putString(key, value) }
    }

    fun getString(key: String, default: String = ""): String {
        return prefs.getString(key, default) ?: default
    }

    fun putInt(key: String, value: Int) {
        prefs.edit { putInt(key, value) }
    }

    fun getInt(key: String, default: Int = 0): Int {
        return prefs.getInt(key, default)
    }

    fun putBool(key: String, value: Boolean) {
        prefs.edit { putBoolean(key, value) }
    }

    fun getBool(key: String, default: Boolean = true): Boolean {
        return prefs.getBoolean(key, default)
    }

    fun remove(key: String) {
        prefs.edit { remove(key) }
    }

    fun clear() {
        prefs.edit { clear() }
    }

    // Save list of devices
    fun saveDevices(devices: List<SavedDevice>) {
        val jsonString = json.encodeToString(devices)
        putString(SAVED_DEVICES_KEY, jsonString)
    }

    // Get list of devices
    fun getDevices(): List<SavedDevice> {
        val jsonString = getString(SAVED_DEVICES_KEY, "[]")
        return try {
            if (jsonString == "[]") {
                emptyList()
            } else {
                json.decodeFromString<List<SavedDevice>>(jsonString)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Add a single device to the list
    fun addDevice(device: SavedDevice) {
        val devices = getDevices().toMutableList()
        // Check if device already exists (by IP address)
        val existingIndex = devices.indexOfFirst { it.ipAddress == device.ipAddress }
        if (existingIndex != -1) {
            // Update existing device
            devices[existingIndex] = device
        } else {
            // Add new device
            devices.add(device)
        }
        saveDevices(devices)
    }

    // Remove a device by IP address
    fun removeDevice(ipAddress: String) {
        val devices = getDevices().toMutableList()
        devices.removeAll { it.ipAddress == ipAddress }
        saveDevices(devices)
    }
} 