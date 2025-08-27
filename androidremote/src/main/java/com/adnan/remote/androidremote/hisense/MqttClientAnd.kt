package com.adnan.remote.androidremote.hisense

import android.content.Context
import android.util.Log
import com.adnan.remote.androidremote.ConnectionListener
import com.adnan.remote.androidremote.R
import com.adnan.remote.androidremote.allowToUse
import com.adnan.remote.androidremote.hisense.TopicProvider.getTopicActionsApplistPath
import com.adnan.remote.androidremote.hisense.TopicProvider.getTopicActionsAuthenticationCodeClosePath
import com.adnan.remote.androidremote.hisense.TopicProvider.getTopicActionsGetTVStatePath
import com.adnan.remote.androidremote.hisense.TopicProvider.getTopicActionsSendKeyPath
import com.adnan.remote.androidremote.hisense.TopicProvider.getTopicActionsSendTextPath
import com.adnan.remote.androidremote.hisense.TopicProvider.getTopicDataApplistPath
import com.adnan.remote.androidremote.hisense.TopicProvider.getTopicDataAuthenticationCodeClosePath
import com.adnan.remote.androidremote.hisense.TopicProvider.getTopicDataAuthenticationCodePath
import com.adnan.remote.androidremote.hisense.TopicProvider.getTopicDataAuthenticationPath
import com.adnan.remote.androidremote.hisense.TopicProvider.getTopicDataGetTokenPath
import com.adnan.remote.androidremote.hisense.TopicProvider.getTopicDataSourcelistPath
import com.adnan.remote.androidremote.hisense.TopicProvider.getTopicDataTokenIssuancePath
import com.adnan.remote.androidremote.hisense.TopicProvider.getTopicStatePath
import com.adnan.remote.androidremote.models.App
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.fusesource.hawtbuf.Buffer
import org.fusesource.hawtbuf.UTF8Buffer
import org.fusesource.mqtt.client.Callback
import org.fusesource.mqtt.client.CallbackConnection
import org.fusesource.mqtt.client.Listener
import org.fusesource.mqtt.client.MQTT
import org.fusesource.mqtt.client.QoS
import org.fusesource.mqtt.client.Topic
import org.json.JSONArray
import org.json.JSONObject
import java.math.BigInteger
import java.nio.charset.StandardCharsets
import java.util.Timer
import java.util.TimerTask
import java.util.regex.Matcher
import java.util.regex.Pattern


private const val TAG = "MqttClientAnd"

class MqttClientAnd(
    val context: Context,
    val tvIp: String,
    val connectionListener: ConnectionListener
) {
    var serviceConfig: VidaaServiceConfig? = null
    var client: MQTT? = null
    var callBack: CallbackConnection? = null
    var preferencesManager = PreferencesManager(context)

    /*  private val _connectionEvents = MutableSharedFlow<ConnectionState>()
      val connectionEvents = _connectionEvents.asSharedFlow()
     */
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    var listOfApps: List<App>? = null


    init {
        serviceConfig = preferencesManager.loadServiceConfig(tvIp)
        if (serviceConfig == null) {
            defineHashes()
        }

        connect()
    }


    val listener = object : Listener {
        override fun onConnected() {
            Log.i(TAG, "onConnected: ")
        }

        override fun onDisconnected() {
            Log.i(TAG, "onDisconnected: ")
        }

        override fun onPublish(
            topic: UTF8Buffer?,
            body: Buffer?,
            ack: Runnable?
        ) {
            Log.i(TAG, "onPublish:  $topic : :  $body")
            topic?.let {
                body?.let {
                    messageArrived(topic, body)
                }
            }
        }


        override fun onFailure(value: Throwable?) {
            Log.i(TAG, "onFailure: $value")
        }
    }


    private fun connect() {
        if (callBack != null)
            return
        scope.launch {
            //  _connectionEvents.emit(ConnectionState.Connecting)

            client = getMQTTClientBuilder()
            callBack = client?.callbackConnection()
            suspendCancellableCoroutine {

                callBack?.listener(listener)
                    ?.connect(object : Callback<Void> {
                        override fun onSuccess(value: Void?) {
                            Log.i(TAG, "onSuccess: ")
                            TopicProvider.setClientID(serviceConfig?.clientID!!)
                            if (serviceConfig?.credentials == null) {

                                subscribeTopics(
                                    callBack!!,
                                    TopicProvider.getLoginTopics(),
                                    object : Callback<ByteArray> {
                                        override fun onSuccess(value: ByteArray?) {
                                            //  cont.resume(ConnectionState.Pairing)
                                            connectionListener.onPinRequired(4)

                                            //  _connectionEvents.emit(ConnectionState.Pairing)
                                        }

                                        override fun onFailure(value: Throwable?) {
                                            connectionListener.onFailedToConnect(
                                                value?.message ?: "Error while connecting"
                                            )
                                            //  cont.resume(ConnectionState.Error("onFailure: $value"))
                                            //  _connectionEvents.emit(ConnectionState.Error("onFailure: $value"))
                                        }
                                    }
                                )

                                invokePairing(callBack!!)
                            } else {
                                subscribeTopics(
                                    callBack!!,
                                    TopicProvider.getWorkTopics(),
                                    object : Callback<ByteArray> {
                                        override fun onSuccess(value: ByteArray?) {
                                            connectionListener.onConnect()
                                            //  cont.resume(ConnectionState.Connected)
                                            //_connectionEvents.emit(ConnectionState.Connected)
                                            getAppList()
                                        }

                                        override fun onFailure(value: Throwable?) {
                                            //     cont.resume(ConnectionState.Error("onFailure: $value"))
                                            connectionListener.onFailedToConnect("Error while connecting $value")
                                            // _connectionEvents.emit(ConnectionState.Error("onFailure: $value"))
                                        }

                                    })
                                sendBytes(
                                    callBack, getTopicActionsGetTVStatePath(),
                                    bArr = null
                                );

                                //getTVState()
                                Timer().schedule(object : TimerTask() {
                                    override fun run() {
                                        //     reportConnected()
                                    }
                                }, 2000)
                            }

                        }

                        override fun onFailure(value: Throwable?) {
                            // cont.resume(ConnectionState.Error("onFailure: $value"))
                            connectionListener.onFailedToConnect("Error while connecting $value")
                            //  _connectionEvents.emit(ConnectionState.Error("onFailure: $value"))

                        }
                    })
            }

            //  _connectionEvents.emit(x)
        }
    }

    private fun messageArrived(uTF8Buffer: UTF8Buffer, buffer: Buffer) {
        val str = String(buffer.toByteArray())
        val uTF8Buffer2 = uTF8Buffer.toString()
        if (uTF8Buffer2.equals(getTopicDataAuthenticationCodeClosePath(), ignoreCase = true)) {
            scope.launch {
                //_connectionEvents.emit(ConnectionState.Error("Auth canceled by user"))
                connectionListener.onFailedToConnect("Auth canceled by user")
                endPairing(true)
            }
        } else if (uTF8Buffer2.equals(getTopicDataTokenIssuancePath(), ignoreCase = true)) {
            Log.i(TAG, "messageArrived: process cridentials $str")
            val credentials = Gson().fromJson(str, Credentials::class.java)
            serviceConfig?.credentials = credentials
            serviceConfig?.let { preferencesManager.saveServiceConfig(it, tvIp) }
            endPairing()
        } else if (uTF8Buffer2.equals(getTopicDataAuthenticationCodePath(), ignoreCase = true)) {
            Log.i(TAG, "messageArrived: process pin $str")
            if (1 == JSONObject(str).getInt("result")) {
                getAccessToken(callBack!!);
            } else if (100 == JSONObject(str).getInt("result"))
                scope.launch {
                    //  _connectionEvents.emit(ConnectionState.Error("Invalid PinCode try again in 5 seconds"))

                    connectionListener.pinError("Invalid PinCode try again")
                    delay(4000)
                    connectionListener.onPinRequired(4)

                    //                     _connectionEvents.emit(ConnectionState.Pairing)

                    // cancelPairing()
                } else {
                scope.launch {
//                    _connectionEvents.emit(ConnectionState.Error("Code Error"))

                    connectionListener.onFailedToConnect("Connection Error")
                }
            }

        } else if (uTF8Buffer2.equals(getTopicDataAuthenticationPath(), ignoreCase = true)) {
            Log.i(TAG, "messageArrived: pair required")
            //  onPairingRequired()
        } else if (uTF8Buffer2.equals(getTopicDataSourcelistPath(), ignoreCase = true)) {
            //processExternalListMessage(str)

        } else if (uTF8Buffer2.equals(getTopicDataApplistPath(), ignoreCase = true)) {
            // processAppListMessage(str)
            processAppListMessage(str)

        } else if (uTF8Buffer2.equals(getTopicStatePath(), ignoreCase = true)) {
            Log.i(TAG, "messageArrived: tv state $str")
            // processTVState(str)
        }


        /*else if (uTF8Buffer2.equals(getTopicDataGetVolumePath(), ignoreCase = true)) {
            processVolume(str)
        } else if (uTF8Buffer2.equals(getTopicDataGetTextPath(), ignoreCase = true)) {
            processKeyboard(str)
        }*/
    }

    private fun endPairing(canceled: Boolean = false) {
        val callback = object : Callback<Void?> {
            override fun onSuccess(value: Void?) {
                if (!canceled)
                    connect()
            }

            override fun onFailure(value: Throwable?) {
                //   TODO("Not yet implemented")
            }
        }

        sendBytes(
            callBack,
            getTopicActionsAuthenticationCodeClosePath(),
            null as ByteArray?,
            object : Callback<Void?> {
                override fun onSuccess(voidR: Void?) {
                    disconnectLoginClient(callback)
                }

                override fun onFailure(th: Throwable?) {
                    disconnectLoginClient(callback)
                }
            })
    }


    private fun getMQTTClientBuilder(): MQTT {
        val mqtt = MQTT()
        try {
            //Context context = DiscoveryManager.getInstance().getContext();
            mqtt.setHost("ssl://$tvIp:36669")
            mqtt.setUserName(serviceConfig?.userName)
            mqtt.setPassword(serviceConfig?.credentials?.accesstoken ?: serviceConfig?.password)
            mqtt.setClientId(serviceConfig?.clientID)
            mqtt.setSslContext(SSLUtils.setupSSLContext(preferencesManager.context, R.raw.hisense))
        } catch (e: Exception) {
            Log.i(TAG, "getMQTTClientBuilder: $e")
        }
        return mqtt
    }


    fun defineHashes() = try {
        val mac = Util.formatMACAddressTo48Bit(Util.generateRandom48BitInteger())
        val timestamp = System.currentTimeMillis() / 1000
        val authHash = Util.computeMD5("38D65DC30F45109A369A86FCE866A85B$$mac")
        val code =
            Util.computeMD5("his${Util.crossSum(timestamp) % 10}h*i&s%e!r^v0i1c9").uppercase()
                .substring(0, 6)
        val passwordHash = Util.computeMD5("$timestamp$$code").uppercase()


        val str: String = "$mac\$his$" + authHash.uppercase()
            .substring(0, 6) + "_vidaacommon_001"


        serviceConfig = VidaaServiceConfig().apply {
            userName = "his$$timestamp"
            password = passwordHash
            clientID = str
        }
    } catch (e: Exception) {
        Log.e(TAG, "Error defining hashes", e)
    }


    fun subscribeTopics(
        connection: CallbackConnection,
        topicNames: Array<String>,
        callback: Callback<ByteArray>? = null
    ) {
        // Map topic strings to Topic objects with a default QoS level
        val topics = topicNames.map { topicName ->
            Topic(topicName, QoS.AT_MOST_ONCE)
        }.toTypedArray()

        // Define fallback callback in case none was provided
        val finalCallback = callback ?: object : Callback<ByteArray> {
            override fun onSuccess(value: ByteArray?) {
                // Default: do nothing or log
            }

            override fun onFailure(value: Throwable?) {
                // Default: do nothing or log
            }
        }

        try {
            connection.subscribe(topics, finalCallback)
        } catch (e: Exception) {
            // Optionally log or handle the error here
            e.printStackTrace()
        }
    }


    /* access modifiers changed from: private */
    fun disconnectLoginClient(callback: Callback<Void?>) {
        if (this.client != null) {
            unsubscribeTopics(callBack!!, TopicProvider.getLoginTopics(), callback)
            callBack?.disconnect(null as Callback<Void?>?)
            callBack = null
            client = null
        }
    }


    /* access modifiers changed from: private */
    fun disconnectAllClient() {
        if (this.client != null) {
            callBack?.suspend()

//            callBack?.disconnect(null as Callback<Void?>?)
            callBack = null
            client = null
        }
    }


    private fun unsubscribeTopics(
        callbackConnection: CallbackConnection,
        strArr: Array<String>,
        callback: Callback<Void?>
    ) {
        var callback = callback

        // Map topic strings to Topic objects with a default QoS level
        val utf8BufferArr: Array<UTF8Buffer> = strArr
            .map { UTF8Buffer(it) }
            .toTypedArray()

        try {
            callbackConnection.unsubscribe(utf8BufferArr, callback)
        } catch (e: Exception) {
            Log.i(TAG, "unsubscribeTopics: $e")
        }
    }

    /* access modifiers changed from: private */
    fun invokePairing(callbackConnection: CallbackConnection) {
        try {
            val jSONObject = JSONObject()
            jSONObject.put("app_version", 2)
            jSONObject.put("connect_result", 0)
            jSONObject.put("device_type", "Kraftwerk9")
            sendJSON(
                callbackConnection,
                TopicProvider.getTopicActionsVidaaAppConnectPath(),
                jSONObject
            )
        } catch (e: java.lang.Exception) {
            Log.i(TAG, "invokePairing: ${e.message}")
        }
    }


    fun sendPairingKey(str: String?) {
        if (str == null || str.isEmpty()) {
            // cancelPairing(Error("User has entered empty pairing pin code"))
            return
        }
        try {
            val jSONObject = JSONObject()
            jSONObject.put("authNum", BigInteger(str))
            sendJSON(
                callBack,
                TopicProvider.getTopicActionsAuthenticationCodePath(),
                jSONObject
            )
        } catch (e: java.lang.Exception) {
            Log.i(TAG, "sendPairingKey: $e")
        }
    }


    private fun getAccessToken(callbackConnection: CallbackConnection) {
        var str: String?
        try {
            str = ""
            val jSONObject2 = JSONObject()
            jSONObject2.put("refreshtoken", str)
            sendJSON(callbackConnection, getTopicDataGetTokenPath(), jSONObject2)
        } catch (e: java.lang.Exception) {
            Log.i(TAG, "getAccessToken: $e")
        }
    }


    fun sendKeyCode(str: String) {

        if (allowToUse())
            sendBytes(
                callBack,
                getTopicActionsSendKeyPath(),
                str.utf8Bytes
            );
        else Log.i("Command", " Hisense sendKey: socket error")

    }


    fun getAppList() {

        sendBytes(callBack, getTopicActionsApplistPath(), null)

    }


    private fun processAppListMessage(str: String?) {
        try {
            val jSONArray = JSONArray(str)
            val arrayList: ArrayList<App> = ArrayList(jSONArray.length())
            for (i in 0..<jSONArray.length()) {
                val jSONObject = jSONArray.getJSONObject(i)
                val appInfo = App(1, "", "")
                appInfo.id = (jSONObject.getString("appId"))

                appInfo.url = (jSONObject.getString("url"))
                appInfo.name = (jSONObject.getString("name"))
                appInfo.icon = (getIconURL(jSONObject.getString("httpIcon")).toString())
                arrayList.add(appInfo)
            }
            listOfApps = arrayList
            //  Util.postSuccess(this.appListListener, arrayList)
            // this.appListListener = null
        } catch (unused: java.lang.Exception) {
        }
    }

    private fun getIconURL(str: String): String? {
        val matcher: Matcher = Pattern.compile("https?://\\S+").matcher(str)
        if (matcher.find()) {
            return matcher.group()
        }
        return ""
    }


    fun launchApp(
        app: App
    ) {
        try {
            val jSONObject = JSONObject()

            jSONObject.put("appId", app.id)
            jSONObject.put("name", app.name)
            jSONObject.put("url", app.url)


            if (allowToUse())
                launchWithJSON(jSONObject)
            else Log.i("Command", ": socket error")

        } catch (e: java.lang.Exception) {
            Log.i(TAG, "launchApp: $e")
            // Util.postError(appLaunchListener, ServiceCommandError.notSupported())
        }
    }


    private fun launchWithJSON(
        jSONObject: JSONObject
    ) {
        sendJSON(callBack, TopicProvider.getTopicActionsLaunchAppPath(), jSONObject)
    }


    fun sendText(str: String?) {
        var str = str
        if (str != null && !str.isEmpty()) {
            if (str == " ") {
                str = "SPACE"
            }
            sendBytes(
                callBack,
                getTopicActionsSendTextPath(),
                ("Lit_$str").toByteArray(StandardCharsets.UTF_8)
            )
        }
    }


    private fun sendJSON(
        callbackConnection: CallbackConnection?,
        str: String?,
        jSONObject: JSONObject?
    ) {
        sendBytes(
            callbackConnection,
            str,
            jSONObject?.toString()?.utf8Bytes ?: ByteArray(0),
        )
    }

    private fun sendBytes(
        callbackConnection: CallbackConnection?,
        str: String?,
        bArr: ByteArray?,
        callBack: Callback<Void?>? = null
    ) {
        var bArr = bArr
        if (callbackConnection != null) {
            if (bArr == null) {
                bArr = ByteArray(0)
            }
            callbackConnection.dispatchQueue.execute {
                callbackConnection.publish(
                    str,
                    bArr,
                    QoS.AT_LEAST_ONCE,
                    false,
                    callBack
                )
            }

        }
    }

}


val String.utf8Bytes: ByteArray
    get() = this.toByteArray(StandardCharsets.UTF_8)
