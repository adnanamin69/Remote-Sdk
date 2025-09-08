package com.sdk.remote.androidremote.fire

import android.content.Context
import android.util.Log
import com.sdk.remote.androidremote.ConnectionListener
import com.sdk.remote.androidremote.Token
import com.sdk.remote.androidremote.TokenDb
import com.sdk.remote.androidremote.RemoteNDKHelper
import com.sdk.remote.androidremote.allowToUse
import com.sdk.remote.androidremote.decode
import com.sdk.remote.androidremote.models.App
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
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
import kotlin.apply
import kotlin.collections.any
import kotlin.let
import kotlin.ranges.until
import kotlin.text.isEmpty
import kotlin.text.isNotEmpty
import kotlin.text.isNullOrEmpty
import kotlin.toString

private const val TAG = "PinConnectionUtils"

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
    public val channelsList = mutableListOf<App>()

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
            Log.e(TAG, "trustCertificate: Exception in TrustCertificate: ${e.message}")
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
        if (ipAddress.isEmpty()) {
            Log.e(TAG, "checkFirstTime: Activity or IP is null/empty: $ipAddress")
            return
        }

        try {
            val url = "http://$ipAddress:8009/apps/FireTVRemote" // Endpoint path
            val request = Request.Builder()
                .url(url)
                .post(RequestBody.create(null, ByteArray(0)))
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e(TAG, "onFailure: CheckFirstTime failed: ${e.message}")
                }

                override fun onResponse(call: Call, response: Response) {
                    if (response.isSuccessful) {
                        Log.d(TAG, "onResponse: CheckFirstTime successful")
                        Log.d(TAG, "onResponse: $response")
                        // Handle successful response logic
                        if (tokenDb.getToken() == null)
                            displayPin()
                        else {
                            listener.onConnect()
                            fetchApps()
                        }
                    } else {
                        Log.e(TAG, "onResponse: CheckFirstTime failed with code: ${response.code}")
                    }
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "checkFirstTime: Exception in checkFirstTime: ${e.message}")
        }
    }


    private fun displayPin() {
        if (ipAddress.isEmpty()) {
            Log.e(TAG, "displayPin:<--- IP null---> $ipAddress")
            return
        }

        val jSONObject = JSONObject()
        try {
            jSONObject.put("friendlyName", "Fire Stick Remote")
        } catch (e: Exception) {
            Log.e(TAG, "displayPin:<--- Exception while putting JSON---> ${e.message}")
        }

        val requestBody = RequestBody.create(
            "application/json; charset=utf-8".toMediaTypeOrNull(),
            jSONObject.toString()
        )

        val request = Request.Builder()
            .url("https://$ipAddress:8080/v1/FireTV/pin/display")
            .post(requestBody)
            .addHeader(xApi, api)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                try {
                    val responseJson = JSONObject(response.body?.string() ?: "")
                    Log.d(
                        TAG,
                        "onResponse:<---DisplayPin Response--->  $responseJson\n<---Response Code--->${response.code}"
                    )

                    if (response.code < 200 || response.code >= 300) {
                        Log.e(TAG, "Display PIN failed. Response code: ${response.code}")
                        return
                    }

                    if (responseJson.getString("description") == "OK") {
                        Log.d(TAG, "PIN displayed successfully.")
                        listener.onPinRequired(4)

                        // showPin.invoke(true)
                    } else {
                        Log.e(TAG, "DisplayPin: Unknown response description.")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "onResponse:<---Exception in Response---> ${e.message}")
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "onFailure:<---DisplayPin---> ${e.message}")
            }
        })
    }


    fun verifyPin(pinCode: String) {
        if (ipAddress.isEmpty()) {
            Log.e(TAG, "verifyPin:<---IP null---> $ipAddress")
            return
        }

        val jsonObject = JSONObject().apply {
            put("pin", pinCode)
        }

        val requestBody = RequestBody.create(
            "application/json; charset=utf-8".toMediaType(),
            jsonObject.toString()
        )


        val request = Request.Builder()
            .url("https://$ipAddress:8080/v1/FireTV/pin/verify")
            .post(requestBody)
            .addHeader(xApi, api)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                try {
                    val body = response.body?.string()
                    Log.d(
                        TAG,
                        "onResponse:<---FireStick VerifyPin Response---> $response\n<---Response Code---> ${response.code}"
                    )
                    val description = JSONObject(body).getString("description")


                    if (response.code < 200 || response.code >= 300 || description.isNullOrEmpty()) {
                        listener.onFailedToConnect("Pairing Failed")

                        Log.e(TAG, "verifyPin failed with response code: ${response.code}")
                        // Log additional failure details as needed
                    } else {
                        Log.d(TAG, "onResponse:<---FireStick Generated Token---> $description")
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

                    Log.e(TAG, "onResponse:<---Exception in VerifyPin---> ${e.message}")
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "onFailure:<---FireStick VerifyPin---> ${e.message}")

            }
        })
    }

    fun fireCommand(str2: String) {
        if (allowToUse())
            fireCommand(str2, "")
        else Log.i("Command", " fire sendKey: socket error")

    }

    private fun fireCommand(str2: String, str3: String) {
        if (ipAddress.isEmpty()) {
            Log.e(TAG, "fireCommand:<---Activity OR IP null---> $ipAddress")
            return
        }

        val clientToken = tokenDb.getToken()

        val jsonObject = JSONObject().apply {
            try {
                put("keyActionType", str3)
            } catch (e: JSONException) {
                Log.e(TAG, "keyboardText:<---Exception---> " + e.message)
            }
        }

        if (str2.isEmpty() || clientToken == null) {

            return
        }

        val requestBody: RequestBody = if (str3.isEmpty()) {
            RequestBody.create("application/json; charset=utf-8".toMediaTypeOrNull(), "")
        } else {
            RequestBody.create(
                "application/json; charset=utf-8".toMediaTypeOrNull(),
                jsonObject.toString()
            )
        }

        // Create the request with custom headers and query parameters
        val request = Request.Builder()
            .url("https://$ipAddress:8080/v1/FireTV?action=$str2")  // Add the action as a query parameter
            .post(requestBody)
            .addHeader(xApi, api)  // Static API key
            .addHeader(
                "x-client-token",
                clientToken
            )  // Dynamic client token from SharedPreferences
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                try {
                    val body = response.body?.string()
                    Log.d(
                        TAG,
                        "onResponse:<---FireCommand Success---> Command: $str2 keyAction: $str3\n$body<---Response Code--->${response.code}"
                    )

                    if (response.code in 200..299) {
                        if (str3 != "keyUp") {
                            Log.d(TAG, "onResponse:fireCommand<---KeyAction Down---> $str3")
                        } else {
                            Log.d(TAG, "onResponse:fireCommand<---KeyAction Up---> $str3")
                        }
                        // Add your Firebase logging here (for success)
                    } else {


                        if (str3 != "keyUp") {
                            Log.d(TAG, "onResponse:<---KeyAction Down--->$str3")
                        }
                        // Add your Firebase logging here (for failure)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "onResponse:<---Exception in FireCommand---> ${e.message}")
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "onFailure:<---FireCommand Failure---> ${e.message}")
                // Add your connection lost dialog or retry logic here
                if (str3 != "keyUp" && str3.isNotEmpty()) {
                    Log.d(TAG, "onFailure:fireCommand<---KeyAction Down--->$str3")
                } else {
                    Log.d(TAG, "onFailure:fireCommand<---KeyAction Up--->$str3")
                }
            }
        })
    }


    private fun fetchApps() {

        val clientToken = tokenDb.getToken()

        if (ipAddress.isEmpty() || clientToken.isNullOrEmpty()) {
            println("fetchApps: <---Activity OR IP null---> $ipAddress")
            return
        }

        val url = "https://$ipAddress:8080/v1/FireTV/apps"
        val request = Request.Builder()
            .url(url)
            .addHeader(xApi, api)
            .addHeader(xClient, clientToken)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                println("fetchApps: <---Failure---> ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    println("fetchApps: <---Failed Response Code---> ${response.code}")
                    if (response.code == 403) {
                        tokenDb.resetToken()
                        listener.onDisconnect()
                        //    displayPin(showPin)
                        //   showPin.invoke(PinState.IDEAL)
                    }
                    return
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
            }
        })
    }


    fun launchApps(packageName: String) {

        val clientToken = tokenDb.getToken()
        if (ipAddress.isEmpty() || clientToken.isNullOrEmpty()) {
            println("launchApps: <---Activity OR IP null---> $ipAddress")
            return
        }

        val url = "https://$ipAddress:8080/v1/FireTV/app/$packageName"
        val requestBody = "".toRequestBody("application/json; charset=utf-8".toMediaType())

        val request = Request.Builder()
            .url(url)
            .addHeader(xApi, api)
            .addHeader(xClient, clientToken)
            .post(requestBody)
            .build()
        if (allowToUse())
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    println("launchApps: <---Failure---> ${e.message}")
                }

                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        println("launchApps: <---Failed Response Code---> ${response.code}")
                        println("launchApps: <---Failed Response Code---> ${response.message}")
                        return
                    }

                    response.body?.string()?.let { responseBody ->
                        try {
                            val responseJson = JSONObject(responseBody)
                            println("launchApps: <---Success Response---> $responseJson")
                        } catch (e: Exception) {
                            println("launchApps: <---Exception in Parsing Response---> ${e.message}")
                        }
                    }
                }
            })
        else Log.i("Command", " fire sendKey: socket error")

    }


    fun fireText(str2: String) {
        if (ipAddress.isEmpty()) {
            Log.e(TAG, "fireCommand:<---Activity OR IP null---> $ipAddress")
            return
        }

        val clientToken = tokenDb.getToken()

        val jsonObject = JSONObject().apply {
            try {
                put("text", str2)
            } catch (e: JSONException) {
                Log.e(
                    TAG,
                    "keyboardText:<---Exception---> " + e.message
                )
            }
        }

        if (str2.isEmpty() || clientToken == null) {

            return
        }

        val requestBody: RequestBody =
            RequestBody.create(
                "application/json; charset=utf-8".toMediaTypeOrNull(),
                jsonObject.toString()
            )


        // Create the request with custom headers and query parameters
        val request = Request.Builder()
            .url("https://$ipAddress:8080/v1/FireTV/text")  // Add the action as a query parameter
            .post(requestBody)
            .addHeader(xApi, api)  // Static API key
            .addHeader(
                "x-client-token",
                clientToken
            )  // Dynamic client token from SharedPreferences
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                try {
                    val body = response.body?.string()
                    Log.d(
                        TAG,
                        "onResponse:<---FireCommand Success---> Command: $str2 keyAction: $str2\n$body<---Response Code--->${response.code}"
                    )

                } catch (e: Exception) {
                    Log.e(
                        TAG,
                        "onResponse:<---Exception in FireCommand---> ${e.message}"
                    )
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                Log.e(
                    TAG,
                    "onFailure:<---FireCommand Failure---> ${e.message}"
                )

            }
        })
    }


    fun fireVoice(action: String = "start") {
        if (ipAddress.isEmpty()) {
            Log.e(TAG, "fireVoice:<---Activity OR IP null---> $ipAddress")
            return
        }

        val clientToken = tokenDb.getToken()
        if (clientToken == null) {
            Log.e(TAG, "fireVoice:<---Missing token or empty audio--->")
            return
        }


        // val requestBody = audioBytes.toRequestBody("audio/pcm".toMediaTypeOrNull())

        val requestBody =
            RequestBody.create("application/json; charset=utf-8".toMediaTypeOrNull(), "")

        val request = Request.Builder()
            .url("https://$ipAddress:8080/v1/FireTV/voiceCommand?action=$action") // action=voice
            .post(requestBody)
            .addHeader(xApi, api)
            .addHeader(xClient, clientToken)
            //   .addHeader("Content-Type", "audio/pcm")  // Make sure it matches actual format
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                try {
                    val body = response.body?.string()
                    Log.d(
                        TAG,
                        "onResponse:<---FireVoice Success--->\n$body<---Response Code--->${response.code}"
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "onResponse:<---Exception in FireVoice---> ${e.message}")
                    Log.i(TAG, "onResponse: $e")

                }
            }

            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "onFailure:<---FireVoice Failure---> ${e.message}")
            }
        })
    }


    fun setVoiceBytes(bytes: ByteArray) {

        // fireVoice("start")
        webSocket.send(ByteString.of(*bytes))

    }
}


fun println(str: String) {
    Log.d(TAG, "println: $str")
}

