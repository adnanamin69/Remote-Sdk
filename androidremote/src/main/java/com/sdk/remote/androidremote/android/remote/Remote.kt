package com.sdk.remote.androidremote.android.remote

// RemoteManager.kt
import android.os.Build
import android.util.Log
import androidx.annotation.Keep
import com.sdk.remote.androidremote.allowToUse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import remote.Remotemessage.RemoteDirection
import remote.Remotemessage.RemoteImeBatchEdit
import remote.Remotemessage.RemoteMessage
import java.io.BufferedInputStream
import java.io.DataInputStream
import java.io.EOFException
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocket

@Keep
class RemoteManager(
    private val host: String,
    private val port: Int,
    private val sslContext: SSLContext,
) {

    private lateinit var sslSocket: SSLSocket
    private lateinit var inputStream: InputStream
    private lateinit var outputStream: OutputStream
    private var remoteImeBatchEdit: RemoteImeBatchEdit? = null
    private var sessionId = 0

    interface RemoteEventListener {
        fun onReady()
        fun onCurrentApp(packageName: String)
        fun onVolumeChanged(volume: VolumeInfo)
        fun onPowered(powered: Boolean)
        fun onError(error: Throwable)
        fun onUnpaired()
    }

    data class VolumeInfo(val level: Int, val max: Int, val muted: Boolean)

    private var eventListener: RemoteEventListener? = null

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var receiveJob: Job? = null

    fun setEventListener(listener: RemoteEventListener) {
        this.eventListener = listener
    }

    fun start() {
        scope.launch {
            try {
                sslSocket =
                    sslContext.socketFactory?.createSocket(host, port) as SSLSocket
                sslSocket.let {
                    //  it.connect(InetSocketAddress(host, port), 10000)
                    it.startHandshake()

                    inputStream = it.inputStream
                    outputStream = it.outputStream
                    sendInitialConfiguration()
                    startReceiving()
                }
            } catch (e: Exception) {
                Log.i("MainActivityPairing", "start: ${e.message}")
                eventListener?.onError(e)
                reconnect()
            }
        }
    }

    private fun sendInitialConfiguration() {
        val message = RemoteMessageManager.createRemoteConfigure(
            code1 = 622,
            model = Build.MODEL,
            vendor = Build.MANUFACTURER,
            unknown1 = 1,
            unknown2 = Build.VERSION.RELEASE
        )
        sendMessage(message)
    }


    private fun startReceiving() {
        receiveJob = scope.launch {
            val input = inputStream
            val bufferedInput = DataInputStream(BufferedInputStream(input))

            while (isActive) {
                try {
                    // Read the size prefix (varint)
                    val messageSize = readRawVarint32(bufferedInput)
                    // Read the actual message bytes
                    val messageBytes = ByteArray(messageSize)
                    bufferedInput.readFully(messageBytes)

                    val message = RemoteMessage.parseFrom(messageBytes)
                    handleMessage(message)
                } catch (e: IOException) {
                    eventListener?.onError(e)
                    reconnect()

                    break
                }
            }
        }
    }


    private fun handleMessage(message: RemoteMessage) {

        //Log.i("MainActivityPairing", "handleMessage: $message")

        when {
            message.hasRemoteConfigure() -> {
                eventListener?.onReady()
                Log.i("MainActivityPairing", "handleMessage: ready")
            }

            message.hasRemoteSetActive() -> {
                Log.i("MainActivityPairing", "handleMessage: set active")
                val message = RemoteMessageManager.createSetActive()
                sendMessage(message)
            }


            message.hasRemoteVoiceBegin() -> {
                Log.i(
                    "MainActivityPairing",
                    "handleMessage: RemoteVoiceBegin ${message.remoteVoiceBegin.sessionId}"
                )
                sessionId = message.remoteVoiceBegin.sessionId
                //   Log.i("VoiceSession", "voice id new: ${message.remoteVoiceBegin.sessionId}")
                activateVoice1()
            }


            message.hasRemoteVoiceEnd() -> {
                Log.i("MainActivityPairing", "handleMessage: RemoteVoiceEnd")
            }

            message.hasRemoteImeBatchEdit() -> {
                Log.i("MainActivityPairing", "handleMessage: RemoteImeBatchEdit here 111")
                remoteImeBatchEdit = message.remoteImeBatchEdit

            }

            message.hasRemoteSetVolumeLevel() -> {
                val volume = message.remoteSetVolumeLevel
                eventListener?.onVolumeChanged(
                    VolumeInfo(
                        volume.volumeLevel,
                        volume.volumeMax,
                        volume.volumeMuted
                    )
                )
            }
            // Handle other message types...
        }
    }


    @Throws(IOException::class)
    private fun readRawVarint32(input: InputStream): Int {
        var result = 0
        var shift = 0
        while (shift < 32) {
            val b = input.read()
            if (b == -1) throw EOFException("EOF while reading varint")
            result = result or ((b and 0x7F) shl shift)
            if ((b and 0x80) == 0) return result
            shift += 7
        }
        throw IOException("Varint too long")
    }


    fun sendKey(keyCode: Int) {


        val message = RemoteMessageManager.createRemoteKeyInject(RemoteDirection.SHORT, keyCode)

        if (allowToUse())
            sendMessage(message)
        else Log.i("Command", "sendKey: socket error")

    }

    fun launchApp(link: String) {
        val message = RemoteMessageManager.createAppLaunch(link)
        if (allowToUse())
            sendMessage(message)
        else Log.i("Command", "sendKey: socket error")

    }

    fun sendText(text: String) {
        var imeCounter = 0
        var fieldCounter = 0

        remoteImeBatchEdit?.let {
            imeCounter = it.imeCounter
            fieldCounter = it.fieldCounter
            Log.i("MainActivityPairing", "remote batch is not empty")

        }
        Log.i("MainActivityPairing", "sendText: $text")

        val message = RemoteMessageManager.createRemoteImeBatchEdit(text, imeCounter, fieldCounter)

        Log.i("MainActivityPairing", "sendText message: $message")

        sendMessage(message)
    }


    fun activateVoice() {
        /*   Log.i("VoiceSession", "activateVoice: $sessionId")
           val message = RemoteMessageManager.createRemoteVoiceActivate(sessionId)
           sendMessage(message)*/
    }

    fun activateVoice1() {
        Log.i("VoiceSession", "activateVoice: $sessionId")
        val message = RemoteMessageManager.createRemoteVoiceActivate(sessionId)
        sendMessage(message)
    }

    fun endVoice() {
        Log.i("VoiceSession", "endVoice: $sessionId")
        val message = RemoteMessageManager.createVoiceEnd(sessionId)
        sendMessage(message)
    }

    fun sendVoice(voicePayload: ByteArray) {

        val message = RemoteMessageManager.createVoicePayload(sessionId, value = voicePayload)
        //Log.i("MainActivityPairing", "sendVoice: $message")

        Log.i("VoiceSession", "sendVoice: $sessionId")

        sendMessage(message)

    }

    private fun sendMessage(message: RemoteMessage) {
        scope.launch {
            try {
                message.writeDelimitedTo(outputStream)
                outputStream.flush()
            } catch (e: IOException) {
                Log.i("MainActivityPairing", "sendMessage: ${e.message}")
                eventListener?.onError(e)
            } catch (e: Exception) {
                Log.i("MainActivityPairing", "sendMessage: ${e.message}")
                eventListener?.onError(e)
            }
        }
    }


    private fun reconnect() {
        stop()
        scope.launch {
            delay(1000)
            start()
        }
    }

    fun stop() {
        receiveJob?.cancel()
        try {
            sslSocket.close()
        } catch (_: IOException) {
            // Handle close error
        }
    }
}


