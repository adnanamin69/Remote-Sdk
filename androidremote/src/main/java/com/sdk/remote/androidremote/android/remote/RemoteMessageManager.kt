package com.sdk.remote.androidremote.android.remote

import androidx.annotation.Keep
import com.google.protobuf.ByteString
import remote.Remotemessage
import remote.Remotemessage.RemoteConfigure
import remote.Remotemessage.RemoteDeviceInfo
import remote.Remotemessage.RemoteDirection
import remote.Remotemessage.RemoteImeBatchEdit
import remote.Remotemessage.RemoteKeyInject
import remote.Remotemessage.RemoteMessage

@Keep
// RemoteMessageManager.kt
object RemoteMessageManager {
    fun createRemoteConfigure(
        code1: Int,
        model: String,
        vendor: String,
        unknown1: Int,
        unknown2: String
    ): RemoteMessage {
        return RemoteMessage.newBuilder()
            .setRemoteConfigure(
                RemoteConfigure.newBuilder()
                    .setCode1(code1)
                    .setDeviceInfo(
                        RemoteDeviceInfo.newBuilder()
                            .setModel(model)
                            .setVendor(vendor)
                            .setUnknown1(unknown1)
                            .setUnknown2(unknown2)
                            .setPackageName("androidtv-remote")
                            .setAppVersion("1.0.0")
                            .build()
                    )
                    .build()
            )
            .build()
    }

    fun createRemoteKeyInject(direction: RemoteDirection, keyCode: Int): RemoteMessage {
        return RemoteMessage.newBuilder()
            .setRemoteKeyInject(
                RemoteKeyInject.newBuilder()
                    .setKeyCode(keyCode)
                    .setDirection(direction)
                    .build()
            )
            .build()
    }

    fun createAppLaunch(link: String): RemoteMessage {
        return RemoteMessage.newBuilder()
            .setRemoteAppLinkLaunchRequest(
                Remotemessage.RemoteAppLinkLaunchRequest.newBuilder()
                    .setAppLink(link)
                    .build()
            ).build()
    }


    fun createRemoteImeBatchEdit(
        text: String,
        imeCounter: Int,
        fieldCounter: Int
    ): RemoteMessage {
        val textFieldStatus = Remotemessage.RemoteImeObject.newBuilder()
            .setStart(text.length - 1)
            .setEnd(text.length - 1)
            .setValue(text)
            .build()

        val editInfo = Remotemessage.RemoteEditInfo.newBuilder()
            .setInsert(0)
            .setTextFieldStatus(textFieldStatus)
            .build()

        val remoteImeBatchEdit = RemoteImeBatchEdit.newBuilder()
            .setImeCounter(imeCounter)
            .setFieldCounter(fieldCounter)
            .addEditInfo(editInfo)
            .build()

        return RemoteMessage.newBuilder()
            .setRemoteImeBatchEdit(remoteImeBatchEdit)
            .build()
    }


    fun createSetActive(): RemoteMessage {
        return RemoteMessage.newBuilder()
            .setRemoteSetActive(
                Remotemessage.RemoteSetActive.newBuilder()
                    .setActive(622)
                    .build()
            ).build()
    }


    fun createRemoteVoiceActivate(session: Int): RemoteMessage {
        return RemoteMessage.newBuilder()
            .setRemoteVoiceBegin(
                Remotemessage.RemoteVoiceBegin.newBuilder()
                    .setSessionId(session)
                    .build()
            ).build()
    }

    fun createVoicePayload(session: Int, value: ByteArray): RemoteMessage {
        return RemoteMessage.newBuilder()
            .setRemoteVoicePayload(
                Remotemessage.RemoteVoicePayload.newBuilder()
                    .setVoicePayload(ByteString.copyFrom(value))
                    .setSessionId(session)
                    .build()
            ).build()
    }


    fun createVoiceEnd(session: Int): RemoteMessage {
        return RemoteMessage.newBuilder()
            .setRemoteVoiceEnd(
                Remotemessage.RemoteVoiceEnd.newBuilder()
                    .setSessionId(session)
                    .build()
            ).build()
    }

}