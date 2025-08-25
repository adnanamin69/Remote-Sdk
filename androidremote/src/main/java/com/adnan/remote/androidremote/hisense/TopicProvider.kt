package com.adnan.remote.androidremote.hisense

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
        "/remoteapp/tv/ui_service/${getClientID()}"

    fun getTopicTVPSBasePath(): String =
        "/remoteapp/tv/platform_service/${getClientID()}"

    fun getTopicMobiBasePath(): String =
        "/remoteapp/mobile/${getClientID()}"

    fun getTopicBrcsBasePath(): String =
        "/remoteapp/mobile/broadcast"

    fun getTopicRemoBasePath(): String =
        "/remoteapp/tv/remote_service/${getClientID()}"

    fun getTopicActionsSendKeyPath(): String =
        "${getTopicRemoBasePath()}/actions/sendkey"

    fun getTopicDataAuthenticationPath(): String =
        "${getTopicMobiBasePath()}/ui_service/data/authentication"

    fun getTopicDataAuthenticationCodePath(): String =
        "${getTopicMobiBasePath()}/ui_service/data/authenticationcode"

    fun getTopicDataAuthenticationCodeClosePath(): String =
        "${getTopicMobiBasePath()}/ui_service/data/authenticationcodeclose"

    fun getTopicActionsAuthenticationCodeClosePath(): String =
        "${getTopicTVUIBasePath()}/actions/authenticationcodeclose"

    fun getTopicActionsAuthenticationCodePath(): String =
        "${getTopicTVUIBasePath()}/actions/authenticationcode"

    fun getTopicDataHotelModeChangePath(): String =
        "${getTopicBrcsBasePath()}/ui_service/data/hotelmodechange"

    fun getTopicDataTokenIssuancePath(): String =
        "${getTopicMobiBasePath()}/platform_service/data/tokenissuance"

    fun getTopicActionsGetTVStatePath(): String =
        "${getTopicTVUIBasePath()}/actions/gettvstate"

    fun getTopicStatePath(): String =
        "${getTopicBrcsBasePath()}/ui_service/state"

    fun getTopicActionsVidaaAppConnectPath(): String =
        "${getTopicTVUIBasePath()}/actions/vidaa_app_connect"

    fun getTopicActionsApplistPath(): String =
        "${getTopicTVUIBasePath()}/actions/applist"

    fun getTopicActionsLaunchAppPath(): String =
        "${getTopicTVUIBasePath()}/actions/launchapp"

    fun getTopicDataApplistPath(): String =
        "${getTopicMobiBasePath()}/ui_service/data/applist"

    fun getTopicActionsChangeSourcePath(): String =
        "${getTopicTVUIBasePath()}/actions/changesource"

    fun getTopicActionsSourcelistPath(): String =
        "${getTopicTVUIBasePath()}/actions/sourcelist"

    fun getTopicDataSourcelistPath(): String =
        "${getTopicMobiBasePath()}/ui_service/data/sourcelist"

    fun getTopicDataGetTokenPath(): String =
        "${getTopicTVPSBasePath()}/data/gettoken"

    fun getTopicActionsSetVolumePath(): String =
        "${getTopicTVPSBasePath()}/actions/changevolume"

    fun getTopicActionsGetVolumePath(): String =
        "${getTopicTVPSBasePath()}/actions/getvolume"

    fun getTopicDataGetVolumePath(): String =
        "${getTopicBrcsBasePath()}/platform_service/actions/volumechange"

    fun getTopicActionsSendTextPath(): String =
        "${getTopicRemoBasePath()}/actions/input"

    fun getTopicDataGetTextPath(): String =
        "${getTopicBrcsBasePath()}/platform_service/actions/bwsinputdata"

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