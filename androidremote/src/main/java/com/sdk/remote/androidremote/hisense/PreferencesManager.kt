package com.sdk.remote.androidremote.hisense

import android.content.Context
import com.google.gson.Gson

const val PREF_NAME = "mqtt_prefs"

class PreferencesManager(val context: Context) {

    fun saveServiceConfig(config: VidaaServiceConfig, ip: String) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)?.edit()?.apply {
            putString(ip, Gson().toJson(config))
            apply()
        }
    }

    fun loadServiceConfig(ip: String): VidaaServiceConfig? {
        val json = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            ?.getString(ip, null)
        return json?.let {
            try {
                Gson().fromJson(it, VidaaServiceConfig::class.java)
            } catch (e: Exception) {
                null
            }
        }
    }
}
