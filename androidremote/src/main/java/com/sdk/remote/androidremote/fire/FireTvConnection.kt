package com.sdk.remote.androidremote.fire

import android.content.Context
import android.util.Log
import com.sdk.remote.androidremote.ConnectionListener
import com.sdk.remote.androidremote.RemoteNDKHelper
import com.sdk.remote.androidremote.Token
import com.sdk.remote.androidremote.TokenDb
import com.sdk.remote.androidremote.allowToUse
import com.sdk.remote.androidremote.decode
import com.sdk.remote.androidremote.models.App
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

private const val TAG = "PinConnectionUtils"

private fun logD(message: String) {
    Log.d(TAG, message)
}

private fun logI(message: String) {
    Log.i(TAG, message)
}

private fun logE(message: String) {
    Log.e(TAG, message)
}

class FireTvConnection(
    val activity: Context,
    private val ipAddress: String,
    val listener: ConnectionListener
) {
    private val xApi: String = RemoteNDKHelper.getXApi().decode()
    private val xClient: String = RemoteNDKHelper.getXClient().decode()
    private val api = RemoteNDKHelper.getApi().decode()


    private lateinit var client: OkHttpClient
    private lateinit var sslContext: SSLContext
    val channelsList = mutableListOf<App>()

    private val tokenDb by lazy {
        TokenDb(activity, Token.FireTv)
    }


    private val webSocket by lazy {
        val token = tokenDb.getToken()


        val request = Request.Builder().url("wss://$ipAddress:9090/")


            .addHeader(xApi, api)
            .addHeader(xClient, token.toString())
            .build()

        client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                super.onOpen(webSocket, response)

                Log.i("VoiceCommand", "onOpen: ")
            }


            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                super.onMessage(webSocket, bytes)
                Log.i("VoiceCommand", "onMessage: ")

            }


            override fun onMessage(webSocket: WebSocket, text: String) {
                super.onMessage(webSocket, text)
                Log.i("VoiceCommand", "onMessage: $text")

            }


            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                super.onFailure(webSocket, t, response)

                Log.i("VoiceCommand", "onFailure: ${t}")
            }
        })
    }


    init {
        trustCertificate()
        checkFirstTime()
    }


    // ---------- Helpers (URLs, Bodies, Guards, Requests) ----------

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private fun buildHttpsUrl(path: String): String = "https://$ipAddress:8080$path"

    private fun emptyBody(): RequestBody = "".toRequestBody(jsonMediaType)

    private fun jsonBody(json: JSONObject): RequestBody =
        json.toString().toRequestBody(jsonMediaType)

    private fun ensureIp(context: String): Boolean {
        if (ipAddress.isEmpty()) {
            logE("$context:<---Activity OR IP null---> $ipAddress")
            return false
        }
        return true
    }

    private fun getClientTokenOrNull(context: String): String? {
        val token = tokenDb.getToken()
        if (token.isNullOrEmpty()) {
            logE("$context:<---Missing token--->")
            return null
        }
        return token
    }

    private fun enqueue(
        url: String,
        method: String = "GET",
        body: RequestBody? = null,
        includeClientHeader: Boolean = false,
        clientHeaderName: String = xClient,
        onResponse: (Response) -> Unit,
        onFailure: (IOException) -> Unit = { e ->
            Log.e(
                TAG,
                "enqueue:<---Failure---> ${e.message}"
            )
        }
    ) {
        val builder = Request.Builder().url(url).addHeader(xApi, api)

        if (includeClientHeader) {
            val token = getClientTokenOrNull("enqueue") ?: return
            builder.addHeader(clientHeaderName, token)
        }

        when (method) {
            "POST" -> builder.post(body ?: emptyBody())
            else -> builder.get()
        }

        client.newCall(builder.build()).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) = onFailure(e)
            override fun onResponse(call: Call, response: Response) = onResponse(response)
        })
    }


    private fun trustCertificate() {
        val trustAllCerts = arrayOf<TrustManager>(
            object : X509TrustManager {
                override fun checkClientTrusted(
                    chain: Array<X509Certificate>,
                    authType: String
                ) {
                }

                override fun checkServerTrusted(
                    chain: Array<X509Certificate>,
                    authType: String
                ) {
                }

                override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
            }
        )
        try {
            sslContext = SSLContext.getInstance("SSL").apply {
                init(null, trustAllCerts, SecureRandom())
            }
        } catch (e: Exception) {
            logE("trustCertificate: Exception in TrustCertificate: ${e.message}")
        }
        client = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
            .hostnameVerifier { _, _ -> true }
            .build()
    }

    fun checkFirstTime() {
        if (!ensureIp("checkFirstTime")) return

        try {
            val url = "http://$ipAddress:8009/apps/FireTVRemote" // Endpoint path
            val request = Request.Builder()
                .url(url)
                .post(ByteArray(0).toRequestBody(null))
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    logE("onFailure: CheckFirstTime failed: ${e.message}")
                }

                override fun onResponse(call: Call, response: Response) {
                    if (response.isSuccessful) {
                        logD("onResponse: CheckFirstTime successful")
                        logD("onResponse: $response")
                        // Handle successful response logic
                        if (tokenDb.getToken() == null)
                            displayPin()
                        else {
                            listener.onConnect()
                            fetchApps()
                        }
                    } else {
                        logE("onResponse: CheckFirstTime failed with code: ${response.code}")
                    }
                }
            })
        } catch (e: Exception) {
            logE("checkFirstTime: Exception in checkFirstTime: ${e.message}")
        }
    }


    private fun displayPin() {
        if (!ensureIp("displayPin")) return

        val jSONObject = JSONObject()
        try {
            jSONObject.put("friendlyName", "Fire Stick Remote")
        } catch (e: Exception) {
            logE("displayPin:<--- Exception while putting JSON---> ${e.message}")
        }

        val url = buildHttpsUrl("/v1/FireTV/pin/display")
        enqueue(url, method = "POST", body = jsonBody(jSONObject), onResponse = { response ->
            try {
                val responseJson = JSONObject(response.body?.string() ?: "")
                logD("onResponse:<---DisplayPin Response--->  $responseJson\n<---Response Code--->${response.code}")

                if (response.code < 200 || response.code >= 300) {
                    logE("Display PIN failed. Response code: ${response.code}")
                    return@enqueue
                }

                if (responseJson.getString("description") == "OK") {
                    logD("PIN displayed successfully.")
                    listener.onPinRequired(4)

                    // showPin.invoke(true)
                } else {
                    logE("DisplayPin: Unknown response description.")
                }
            } catch (e: Exception) {
                logE("onResponse:<---Exception in Response---> ${e.message}")
            }
        }, onFailure = { e -> logE("onFailure:<---DisplayPin---> ${e.message}") })
    }


    fun verifyPin(pinCode: String) {
        if (!ensureIp("verifyPin")) return

        val jsonObject = JSONObject().apply {
            put("pin", pinCode)
        }

        val url = buildHttpsUrl("/v1/FireTV/pin/verify")
        enqueue(url, method = "POST", body = jsonBody(jsonObject), onResponse = { response ->
            try {
                val body = response.body?.string()
                logD("onResponse:<---FireStick VerifyPin Response---> $response\n<---Response Code---> ${response.code}")
                val description = JSONObject(body).getString("description")


                if (response.code < 200 || response.code >= 300 || description.isNullOrEmpty()) {
                    listener.onFailedToConnect("Pairing Failed")

                    logE("verifyPin failed with response code: ${response.code}")
                    // Log additional failure details as needed
                } else {
                    logD("onResponse:<---FireStick Generated Token---> $description")
                    // Additional logging or actions based on success can go here
                    tokenDb.saveToken(description)
                    // callback.invoke("Pairing Successful")

                    listener.onConnect()
                    fetchApps()

                    // 0kFk4yc
                    //  wUVTuVY
                }
            } catch (e: Exception) {
                listener.onFailedToConnect("Pairing Failed")

                logE("onResponse:<---Exception in VerifyPin---> ${e.message}")
            }
        }, onFailure = { e -> logE("onFailure:<---FireStick VerifyPin---> ${e.message}") })
    }

    fun fireCommand(str2: String) {
        if (allowToUse())
            fireCommand(str2, "")
        else Log.i("Command", " fire sendKey: socket error")

    }

    private fun fireCommand(str2: String, str3: String) {
        if (!ensureIp("fireCommand")) return

        val clientToken = getClientTokenOrNull("fireCommand") ?: return

        val jsonObject = JSONObject().apply {
            try {
                put("keyActionType", str3)
            } catch (e: JSONException) {
                logE("keyboardText:<---Exception---> " + e.message)
            }
        }

        if (str2.isEmpty()) return

        val url = buildHttpsUrl("/v1/FireTV?action=$str2")
        val body: RequestBody = if (str3.isEmpty()) emptyBody() else jsonBody(jsonObject)

        enqueue(
            url = url,
            method = "POST",
            body = body,
            includeClientHeader = true,
            clientHeaderName = "x-client-token",
            onResponse = { response ->
                try {
                    val body = response.body?.string()
                    logD("onResponse:<---FireCommand Success---> Command: $str2 keyAction: $str3\n$body<---Response Code--->${response.code}")

                    if (response.code in 200..299) {
                        if (str3 != "keyUp") {
                            logD("onResponse:fireCommand<---KeyAction Down---> $str3")
                        } else {
                            logD("onResponse:fireCommand<---KeyAction Up---> $str3")
                        }
                        // Add your Firebase logging here (for success)
                    } else {


                        if (str3 != "keyUp") {
                            logD("onResponse:<---KeyAction Down--->$str3")
                        }
                        // Add your Firebase logging here (for failure)
                    }
                } catch (e: Exception) {
                    logE("onResponse:<---Exception in FireCommand---> ${e.message}")
                }
            },
            onFailure = { e ->
                logE("onFailure:<---FireCommand Failure---> ${e.message}")
                if (str3 != "keyUp" && str3.isNotEmpty()) {
                    logD("onFailure:fireCommand<---KeyAction Down--->$str3")
                } else {
                    logD("onFailure:fireCommand<---KeyAction Up--->$str3")
                }
            }
        )
    }


    private fun fetchApps() {

        val clientToken = getClientTokenOrNull("fetchApps") ?: return
        if (!ensureIp("fetchApps")) return

        val url = buildHttpsUrl("/v1/FireTV/apps")
        enqueue(url, includeClientHeader = true, onResponse = { response ->
            if (!response.isSuccessful) {
                println("fetchApps: <---Failed Response Code---> ${response.code}")
                if (response.code == 403) {
                    tokenDb.resetToken()
                    listener.onDisconnect()
                    //    displayPin(showPin)
                    //   showPin.invoke(PinState.IDEAL)
                }
                return@enqueue
            }
            response.body.string().let { responseBody ->
                try {
                    val jsonArray = JSONArray(responseBody)
                    println("fetchApps: <---Success Response---> $jsonArray")

                    for (i in 0 until jsonArray.length()) {
                        val jsonObject = jsonArray.getJSONObject(i)
                        val channel = App(
                            image = null,
                            activityName = jsonObject.getString("appId"),
                            name = jsonObject.getString("name"),
                            icon = jsonObject.getString("iconArtSmallUri"),
                            pkgName = jsonObject.getString("appId"),
                            content = "",
                            id = "",
                            url = ""
                        )
                        if (!channelsList.any { it.activityName == channel.activityName })
                            channelsList.add(channel)
                    }

                    println("fetchApps: <---Parsed Channels---> $channelsList")
                } catch (e: Exception) {
                    println("fetchApps: <---Exception in Parsing Response---> ${e.message}")
                }
            }
        }, onFailure = { e -> println("fetchApps: <---Failure---> ${e.message}") })
    }


    fun launchApps(packageName: String) {

        val clientToken = getClientTokenOrNull("launchApps") ?: return
        if (!ensureIp("launchApps")) return

        val url = buildHttpsUrl("/v1/FireTV/app/$packageName")
        if (allowToUse())
            enqueue(
                url,
                method = "POST",
                body = emptyBody(),
                includeClientHeader = true,
                onResponse = { response ->
                    if (!response.isSuccessful) {
                        println("launchApps: <---Failed Response Code---> ${response.code}")
                        println("launchApps: <---Failed Response Code---> ${response.message}")
                        return@enqueue
                    }

                    response.body?.string()?.let { responseBody ->
                        try {
                            val responseJson = JSONObject(responseBody)
                            println("launchApps: <---Success Response---> $responseJson")
                        } catch (e: Exception) {
                            println("launchApps: <---Exception in Parsing Response---> ${e.message}")
                        }
                    }
                },
                onFailure = { e -> println("launchApps: <---Failure---> ${e.message}") })
        else Log.i("Command", " fire sendKey: socket error")

    }


    fun fireText(str2: String) {
        if (!ensureIp("fireText")) return

        val clientToken = getClientTokenOrNull("fireText") ?: return

        val jsonObject = JSONObject().apply {
            try {
                put("text", str2)
            } catch (e: JSONException) {
                logE(
                    "keyboardText:<---Exception---> " + e.message
                )
            }
        }

        if (str2.isEmpty()) return

        val requestBody: RequestBody = jsonBody(jsonObject)

        val url = buildHttpsUrl("/v1/FireTV/text")
        enqueue(
            url,
            method = "POST",
            body = requestBody,
            includeClientHeader = true,
            clientHeaderName = "x-client-token",
            onResponse = { response ->
                try {
                    val body = response.body?.string()
                    logD("onResponse:<---FireCommand Success---> Command: $str2 keyAction: $str2\n$body<---Response Code--->${response.code}")

                } catch (e: Exception) {
                    logE("onResponse:<---Exception in FireCommand---> ${e.message}")
                }
            },
            onFailure = { e ->
                logE("onFailure:<---FireCommand Failure---> ${e.message}")
            })
    }


    fun fireVoice(action: String = "start") {
        if (!ensureIp("fireVoice")) return
        val clientToken = getClientTokenOrNull("fireVoice") ?: return


        // val requestBody = audioBytes.toRequestBody("audio/pcm".toMediaTypeOrNull())

        val requestBody = emptyBody()

        val url = buildHttpsUrl("/v1/FireTV/voiceCommand?action=$action")
        enqueue(
            url,
            method = "POST",
            body = requestBody,
            includeClientHeader = true,
            onResponse = { response ->
                try {
                    val body = response.body?.string()
                    logD("onResponse:<---FireVoice Success--->\n$body<---Response Code--->${response.code}")
                } catch (e: Exception) {
                    logE("onResponse:<---Exception in FireVoice---> ${e.message}")
                    logI("onResponse: $e")

                }
            },
            onFailure = { e -> logE("onFailure:<---FireVoice Failure---> ${e.message}") })
    }


    fun setVoiceBytes(bytes: ByteArray) {

        // fireVoice("start")
        webSocket.send(ByteString.of(*bytes))

    }
}


fun println(str: String) {
    logD("println: $str")
}

