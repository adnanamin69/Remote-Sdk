package com.sdk.remote.androidremote.models

import androidx.annotation.Keep

@Keep
data class App(
    var image: Int,
    var name: String,
    var content: String,
    var pkgName: String = "",
    var activityName: String = "",
    var id: String = "",
    var url: String = "",
    var icon: String = ""
)
