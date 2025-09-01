package com.sdk.remote.androidremote.hisense

import androidx.annotation.Keep

@Keep
data class Credentials(
    val accesstoken: String,
    val accesstoken_time: String,
    val accesstoken_duration_day: Int,
    val refreshtoken: String,
    val refreshtoken_time: String,
    val refreshtoken_duration_day: Int
)

@Keep
data class VidaaServiceConfig(
    var userName: String = "",
    var password: String = "",
    var clientID: String = "",
    var credentials: Credentials? = null
)