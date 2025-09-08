package com.sdk.remote.androidremote.hisense

import com.sdk.remote.androidremote.RemoteNDKHelper

object TopicProvider {
    var clientID1: String = ""
    fun setClientID(clientID: String) {
        TopicProvider.clientID1 = clientID
    }

    fun getClientID(): String {
        // Replace with your actual client ID logic
        return clientID1
    }

    fun getTopicTVUIBasePath(): String =
        RemoteNDKHelper.getTopicTVUIBasePath(getClientID())

    fun getTopicTVPSBasePath(): String =
        RemoteNDKHelper.getTopicTVPSBasePath(getClientID())

    fun getTopicMobiBasePath(): String =
        RemoteNDKHelper.getTopicMobiBasePath(getClientID())

    fun getTopicBrcsBasePath(): String =
        RemoteNDKHelper.getTopicBrcsBasePath()

    fun getTopicRemoBasePath(): String =
        RemoteNDKHelper.getTopicRemoBasePath(getClientID())

    fun getTopicActionsSendKeyPath(): String =
        RemoteNDKHelper.getTopicActionsSendKeyPath(getClientID())

    fun getTopicDataAuthenticationPath(): String =
        RemoteNDKHelper.getTopicDataAuthenticationPath(getClientID())

    fun getTopicDataAuthenticationCodePath(): String =
        RemoteNDKHelper.getTopicDataAuthenticationCodePath(getClientID())

    fun getTopicDataAuthenticationCodeClosePath(): String =
        RemoteNDKHelper.getTopicDataAuthenticationCodeClosePath(getClientID())

    fun getTopicActionsAuthenticationCodeClosePath(): String =
        RemoteNDKHelper.getTopicActionsAuthenticationCodeClosePath(getClientID())

    fun getTopicActionsAuthenticationCodePath(): String =
        RemoteNDKHelper.getTopicActionsAuthenticationCodePath(getClientID())

    fun getTopicDataHotelModeChangePath(): String =
        RemoteNDKHelper.getTopicDataHotelModeChangePath()

    fun getTopicDataTokenIssuancePath(): String =
        RemoteNDKHelper.getTopicDataTokenIssuancePath(getClientID())

    fun getTopicActionsGetTVStatePath(): String =
        RemoteNDKHelper.getTopicActionsGetTVStatePath(getClientID())

    fun getTopicStatePath(): String =
        RemoteNDKHelper.getTopicStatePath()

    fun getTopicActionsVidaaAppConnectPath(): String =
        RemoteNDKHelper.getTopicActionsVidaaAppConnectPath(getClientID())

    fun getTopicActionsApplistPath(): String =
        RemoteNDKHelper.getTopicActionsApplistPath(getClientID())

    fun getTopicActionsLaunchAppPath(): String =
        RemoteNDKHelper.getTopicActionsLaunchAppPath(getClientID())

    fun getTopicDataApplistPath(): String =
        RemoteNDKHelper.getTopicDataApplistPath(getClientID())

    fun getTopicActionsChangeSourcePath(): String =
        RemoteNDKHelper.getTopicActionsChangeSourcePath(getClientID())

    fun getTopicActionsSourcelistPath(): String =
        RemoteNDKHelper.getTopicActionsSourcelistPath(getClientID())

    fun getTopicDataSourcelistPath(): String =
        RemoteNDKHelper.getTopicDataSourcelistPath(getClientID())

    fun getTopicDataGetTokenPath(): String =
        RemoteNDKHelper.getTopicDataGetTokenPath(getClientID())

    fun getTopicActionsSetVolumePath(): String =
        RemoteNDKHelper.getTopicActionsSetVolumePath(getClientID())

    fun getTopicActionsGetVolumePath(): String =
        RemoteNDKHelper.getTopicActionsGetVolumePath(getClientID())

    fun getTopicDataGetVolumePath(): String =
        RemoteNDKHelper.getTopicDataGetVolumePath()

    fun getTopicActionsSendTextPath(): String =
        RemoteNDKHelper.getTopicActionsSendTextPath(getClientID())

    fun getTopicDataGetTextPath(): String =
        RemoteNDKHelper.getTopicDataGetTextPath()

    fun getWorkTopics(): Array<String> = arrayOf(
        getTopicDataSourcelistPath(),
        getTopicDataApplistPath(),
        getTopicStatePath(),
        getTopicDataGetVolumePath(),
        getTopicDataGetTextPath()
    )

    fun getLoginTopics(): Array<String> = arrayOf(
        getTopicDataAuthenticationPath(),
        getTopicDataAuthenticationCodePath(),
        getTopicDataHotelModeChangePath(),
        getTopicDataTokenIssuancePath(),
        getTopicActionsVidaaAppConnectPath(),
        getTopicDataAuthenticationCodeClosePath()
    )
}