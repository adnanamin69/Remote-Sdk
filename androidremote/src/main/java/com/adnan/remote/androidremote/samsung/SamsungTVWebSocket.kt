package com.adnan.remote.androidremote.samsung

import android.annotation.SuppressLint
import android.content.Context
import android.util.Base64
import android.util.Log
import android.view.KeyEvent
import androidx.annotation.Keep
import androidx.core.content.edit
import com.adnan.remote.androidremote.ATVSSLContext
import com.adnan.remote.androidremote.ConnectionListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import org.json.JSONObject
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager

private const val TAG = "SamsungTVWebSocket"

class SamsungTVWebSocket(
    private val context: Context,
    private val ip: String,
    private val connectionListener: ConnectionListener,
) : WebSocketListener() {

    val name: String = "SamsungTvRemote"

    val sslContext: SSLContext? = ATVSSLContext.getInstance(context)
    private var webSocket: WebSocket? = null
    private val client = sslContext?.socketFactory?.let {
        OkHttpClient.Builder()
            .sslSocketFactory(it, getTrustManager())
            .hostnameVerifier { _, _ -> true }
            .pingInterval(30, TimeUnit.SECONDS)
            .build()
    }

    @SuppressLint("TrustAllX509TrustManager")
    private fun getTrustManager(): X509TrustManager {
        return object : X509TrustManager {
            override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
        }
    }

    private val nameEncoded = Base64.encodeToString(name.toByteArray(), Base64.NO_WRAP)

    init {
        connect()
    }

    fun connect() {
        val url: String = getToken()?.let {
            Log.i(TAG, "connect: $it")
            "wss://$ip:8002/api/v2/channels/samsung.remote.control?name=$nameEncoded&token=$it"
        } ?: "wss://$ip:8002/api/v2/channels/samsung.remote.control?name=$nameEncoded"

        if (getToken() == null) {
            connectionListener.onPinRequired(0)
        }

        Log.i(TAG, "connect: $url")
        val request = Request.Builder().url(url).build()
        webSocket = client?.newWebSocket(request, this)
    }

    override fun onOpen(webSocket: WebSocket, response: Response) {
        println("Connected to Samsung TV!")
    }

    fun launchAppViaHttp(appId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val url = "http://$ip:8001/api/v2/applications/$appId"
            val client = OkHttpClient()

            val request = Request.Builder()
                .url(url)
                .post(RequestBody.create(null, ByteArray(0))) // empty POST body
                .build()

            try {
                val response = withContext(Dispatchers.IO) { client.newCall(request).execute() }
                response.isSuccessful
            } catch (e: Exception) {
                Log.e("LaunchAppHttp", "Failed to launch app: ${e.message}")
                false
            }
        }
    }


    fun sendCommand(command: String) {
        val channelRequest = JSONObject().apply {
            put("method", "ms.remote.control")
            put("params", JSONObject().apply {
                put("Cmd", "Click")
                put("DataOfCmd", command)
                put("Option", "false")
                put("TypeOfRemote", "SendRemoteKey")
            })
        }

        val success = webSocket?.send(channelRequest.toString())
        if (success == false) {
            println("Failed to send channel command")
            connect()
        } else {
            println("Channel command sent successfully")
        }
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        println("Message from TV: $text")
        val token = extractTokenFromMessage(text)
        //   showPairCallBack.invoke(false)
        connectionListener.onConnect()
        Log.i(TAG, "onMessage: $token")
        if (!token.isNullOrEmpty()) {
            saveToken(token)
        }
    }

    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
        println("Binary Message from TV: ${bytes.hex()}")
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        webSocket.close(1000, null)
        println("Closing connection: $code / $reason")
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        println("Connection error: ${t.message}")
        response?.let { println("Response: ${it.code} - ${it.message}") }
    }

    fun disconnect() {
        webSocket?.close(1000, "Disconnecting")
    }

    private fun println(str: String) {
        Log.i(TAG, str)
    }

    private fun extractTokenFromMessage(message: String): String? {
        return try {
            val jsonObject = JSONObject(message)
            val dataObject = jsonObject.getJSONObject("data")
            dataObject.optString("token")
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun saveToken(token: String) {

        val sharedPreferences =
            context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        sharedPreferences.edit {
            putString("token", token)
        }

    }

    private fun getToken(): String? {
        val sharedPreferences =
            context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        return sharedPreferences.getString("token", null)
    }
}

@Keep
fun Int.getCommand(): String {
    return when (this) {
        KeyEvent.KEYCODE_VOLUME_UP -> "KEY_VOLUP"
        KeyEvent.KEYCODE_VOLUME_DOWN -> "KEY_VOLDOWN"
        KeyEvent.KEYCODE_BACK -> "KEY_RETURN"
        KeyEvent.KEYCODE_SETTINGS -> "KEY_MENU"
        KeyEvent.KEYCODE_DPAD_UP -> "KEY_UP"
        KeyEvent.KEYCODE_DPAD_DOWN -> "KEY_DOWN"
        KeyEvent.KEYCODE_DPAD_LEFT -> "KEY_LEFT"
        KeyEvent.KEYCODE_DPAD_RIGHT -> "KEY_RIGHT"
        KeyEvent.KEYCODE_DPAD_CENTER -> "KEY_OK"
        KeyEvent.KEYCODE_ENTER -> "KEY_OK"
        KeyEvent.KEYCODE_MUTE -> "KEY_MUTE"
        KeyEvent.KEYCODE_VOLUME_MUTE -> "KEY_MUTE"
        KeyEvent.KEYCODE_POWER -> "KEY_POWER"
        KeyEvent.KEYCODE_MEDIA_FAST_FORWARD -> "KEY_FF"
        KeyEvent.KEYCODE_MEDIA_REWIND -> "KEY_LEFT"
        KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> "KEY_PLAYPAUSE"
        KeyEvent.KEYCODE_MEDIA_STOP -> "KEY_STOP"
        KeyEvent.KEYCODE_MEDIA_NEXT -> "KEY_SKIP"
        KeyEvent.KEYCODE_MEDIA_PREVIOUS -> "KEY_PREV"
        KeyEvent.KEYCODE_HOME -> "KEY_HOME"
        else -> ""
    }
}

@Keep
fun String.samsungId(): String {
    return when (this.lowercase()) {
        "youtube" -> "111299001912"
        "netflix" -> "3201907018807"
        "spotify" -> "3201606009684"
        "prime video" -> "3201910019365"
        else -> "0"
    }

}
