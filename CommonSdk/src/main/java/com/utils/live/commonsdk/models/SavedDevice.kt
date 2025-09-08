package com.utils.live.commonsdk.models

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class SavedDevice(
    val name: String,
    val deviceType: String,
    val ipAddress: String
)
