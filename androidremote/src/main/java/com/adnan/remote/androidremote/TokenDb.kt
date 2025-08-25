package com.adnan.remote.androidremote

import android.content.Context
import androidx.core.content.edit

class TokenDb(val context: Context, val tokenKey: Token) {

    val sharedPreferences by lazy {
        context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
    }


    fun resetToken() {
        sharedPreferences.edit {
            putString(tokenKey.key, null)
        }
    }


    fun saveToken(token: String) {
        if (getToken().isNullOrEmpty()) {
            sharedPreferences.edit {
                putString(tokenKey.key, token)
            }
        }
    }

    fun getToken(): String? {
        return sharedPreferences.getString(tokenKey.key, null)
    }


}