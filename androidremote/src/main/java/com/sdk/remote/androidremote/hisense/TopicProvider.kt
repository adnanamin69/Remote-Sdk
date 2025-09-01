package com.sdk.remote.androidremote.hisense

import com.sdk.remote.androidremote.YourNDKHelper

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
        YourNDKHelper.getTopicTVUIBasePath(getClientID())

    fun getTopicTVPSBasePath(): String =
        YourNDKHelper.getTopicTVPSBasePath(getClientID())

    fun getTopicMobiBasePath(): String =
        YourNDKHelper.getTopicMobiBasePath(getClientID())

    fun getTopicBrcsBasePath(): String =
        YourNDKHelper.getTopicBrcsBasePath()

    fun getTopicRemoBasePath(): String =
        YourNDKHelper.getTopicRemoBasePath(getClientID())

    fun getTopicActionsSendKeyPath(): String =
        YourNDKHelper.getTopicActionsSendKeyPath(getClientID())

    fun getTopicDataAuthenticationPath(): String =
        YourNDKHelper.getTopicDataAuthenticationPath(getClientID())

    fun getTopicDataAuthenticationCodePath(): String =
        YourNDKHelper.getTopicDataAuthenticationCodePath(getClientID())

    fun getTopicDataAuthenticationCodeClosePath(): String =
        YourNDKHelper.getTopicDataAuthenticationCodeClosePath(getClientID())

    fun getTopicActionsAuthenticationCodeClosePath(): String =
        YourNDKHelper.getTopicActionsAuthenticationCodeClosePath(getClientID())

    fun getTopicActionsAuthenticationCodePath(): String =
        YourNDKHelper.getTopicActionsAuthenticationCodePath(getClientID())

    fun getTopicDataHotelModeChangePath(): String =
        YourNDKHelper.getTopicDataHotelModeChangePath()

    fun getTopicDataTokenIssuancePath(): String =
        YourNDKHelper.getTopicDataTokenIssuancePath(getClientID())

    fun getTopicActionsGetTVStatePath(): String =
        YourNDKHelper.getTopicActionsGetTVStatePath(getClientID())

    fun getTopicStatePath(): String =
        YourNDKHelper.getTopicStatePath()

    fun getTopicActionsVidaaAppConnectPath(): String =
        YourNDKHelper.getTopicActionsVidaaAppConnectPath(getClientID())

    fun getTopicActionsApplistPath(): String =
        YourNDKHelper.getTopicActionsApplistPath(getClientID())

    fun getTopicActionsLaunchAppPath(): String =
        YourNDKHelper.getTopicActionsLaunchAppPath(getClientID())

    fun getTopicDataApplistPath(): String =
        YourNDKHelper.getTopicDataApplistPath(getClientID())

    fun getTopicActionsChangeSourcePath(): String =
        YourNDKHelper.getTopicActionsChangeSourcePath(getClientID())

    fun getTopicActionsSourcelistPath(): String =
        YourNDKHelper.getTopicActionsSourcelistPath(getClientID())

    fun getTopicDataSourcelistPath(): String =
        YourNDKHelper.getTopicDataSourcelistPath(getClientID())

    fun getTopicDataGetTokenPath(): String =
        YourNDKHelper.getTopicDataGetTokenPath(getClientID())

    fun getTopicActionsSetVolumePath(): String =
        YourNDKHelper.getTopicActionsSetVolumePath(getClientID())

    fun getTopicActionsGetVolumePath(): String =
        YourNDKHelper.getTopicActionsGetVolumePath(getClientID())

    fun getTopicDataGetVolumePath(): String =
        YourNDKHelper.getTopicDataGetVolumePath()

    fun getTopicActionsSendTextPath(): String =
        YourNDKHelper.getTopicActionsSendTextPath(getClientID())

    fun getTopicDataGetTextPath(): String =
        YourNDKHelper.getTopicDataGetTextPath()

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