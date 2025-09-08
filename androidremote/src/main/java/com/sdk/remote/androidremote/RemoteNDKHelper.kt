package com.sdk.remote.androidremote

import android.content.Context
import androidx.annotation.Keep

@Keep
object RemoteNDKHelper {
    var currentPkg = ""
    fun initialize(context: Context) {
        currentPkg = context.packageName
    }

    fun loadLib() {
        System.loadLibrary("native-lib")
    }

    init {
        loadLib()
    }

    external fun getSecretKey(): String
    external fun getXApi(): String
    external fun getXClient(): String
    external fun getApi(): String

    // SSL/keystore related sensitive values
    external fun getPfxPassword(): String
    external fun getPfxFileName(): String
    external fun getPfxAlias(): String

    @JvmStatic
    external fun getHisenseBksPassword(): String

    // Hisense Topic paths
    external fun getTopicTVUIBasePath(clientId: String): String
    external fun getTopicTVPSBasePath(clientId: String): String
    external fun getTopicMobiBasePath(clientId: String): String
    external fun getTopicBrcsBasePath(): String
    external fun getTopicRemoBasePath(clientId: String): String

    external fun getTopicActionsSendKeyPath(clientId: String): String
    external fun getTopicDataAuthenticationPath(clientId: String): String
    external fun getTopicDataAuthenticationCodePath(clientId: String): String
    external fun getTopicDataAuthenticationCodeClosePath(clientId: String): String
    external fun getTopicActionsAuthenticationCodeClosePath(clientId: String): String
    external fun getTopicActionsAuthenticationCodePath(clientId: String): String
    external fun getTopicDataHotelModeChangePath(): String
    external fun getTopicDataTokenIssuancePath(clientId: String): String
    external fun getTopicActionsGetTVStatePath(clientId: String): String
    external fun getTopicStatePath(): String
    external fun getTopicActionsVidaaAppConnectPath(clientId: String): String
    external fun getTopicActionsApplistPath(clientId: String): String
    external fun getTopicActionsLaunchAppPath(clientId: String): String
    external fun getTopicDataApplistPath(clientId: String): String
    external fun getTopicActionsChangeSourcePath(clientId: String): String
    external fun getTopicActionsSourcelistPath(clientId: String): String
    external fun getTopicDataSourcelistPath(clientId: String): String
    external fun getTopicDataGetTokenPath(clientId: String): String
    external fun getTopicActionsSetVolumePath(clientId: String): String
    external fun getTopicActionsGetVolumePath(clientId: String): String
    external fun getTopicDataGetVolumePath(): String
    external fun getTopicActionsSendTextPath(clientId: String): String
    external fun getTopicDataGetTextPath(): String

    // Allowed package names (Base64-encoded as before)
    external fun getAllowedPackages(): Array<String>

    // SDK and allow checks
    external fun getSupportedSdk(): Int
    external fun isSdkSupported(currentSdk: Int): Boolean
    external fun isPackageAllowedEncoded(encodedPkg: String): Boolean
    external fun allowToUse(encodedPkg: String, currentSdk: Int): Boolean
}
