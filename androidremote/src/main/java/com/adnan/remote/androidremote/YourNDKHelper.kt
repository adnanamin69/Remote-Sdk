package com.adnan.remote.androidremote

import androidx.annotation.Keep

@Keep
object YourNDKHelper {
    fun loadLib() {
        System.loadLibrary("native-lib")
    }

    external fun getSecretKey(): String
    external fun getXApi(): String
    external fun getXClient(): String
    external fun getApi(): String
}
