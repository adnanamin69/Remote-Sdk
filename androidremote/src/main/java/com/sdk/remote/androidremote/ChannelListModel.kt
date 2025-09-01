package com.sdk.remote.androidremote

data class ChannelListModel(
    val appLabelName: String,
    val channelActivityName: String,
    val channelName: String,
    val channelPackageName: String,
    val isAppInstalled: Boolean,
    val res: String
)
