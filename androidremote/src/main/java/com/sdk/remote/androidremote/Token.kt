package com.sdk.remote.androidremote

sealed class Token(val key: String) {
    data object Samsung : Token("samsung")
    data object FireTv : Token("fire_tv")
}
