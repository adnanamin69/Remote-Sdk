package com.sdk.remote.androidremote.android

import android.content.Context
import android.util.Base64
import android.util.Log
import androidx.annotation.Keep
import androidx.core.content.edit
import com.sdk.remote.androidremote.ATVSSLContext
import com.sdk.remote.androidremote.ConnectionListener
import com.sdk.remote.androidremote.android.remote.RemoteManager
import kotlinx.coroutines.CoroutineScope

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.OutputStream
import java.math.BigInteger
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.security.PublicKey
import java.security.cert.Certificate
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.security.interfaces.RSAPublicKey
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocket

@Keep
class PairingManager(
    private val context: Context,
    private val ip: String,
    private val connectionListener: ConnectionListener
) {

    val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val sslContext: SSLContext? = ATVSSLContext.getInstance(context)


    private val PAIRING_MSG_HEADER =
        byteArrayOf(42, 8, 2, 16, 200.toByte(), 1, 194.toByte(), 2, 34, 10, 32)
    private val PAIRING_MSG = byteArrayOf(
        8,
        2,
        16,
        200.toByte(),
        1,
        82,
        43,
        10,
        21,
        105,
        110,
        102,
        111,
        46,
        107,
        111,
        100,
        111,
        110,
        111,
        46,
        97,
        115,
        115,
        105,
        115,
        116,
        97,
        110,
        116,
        18,
        13,
        105,
        110,
        116,
        101,
        114,
        102,
        97,
        99,
        101,
        32,
        119,
        101,
        98
    )
    private val PAIRING_PORT = 6467
    private val OPTION_MSG =
        byteArrayOf(8, 2, 16, 200.toByte(), 1, 162.toByte(), 1, 8, 10, 4, 8, 3, 16, 6, 24, 1)
    private val CONFIGURATION_MSG =
        byteArrayOf(8, 2, 16, 200.toByte(), 1, 242.toByte(), 1, 8, 10, 4, 8, 3, 16, 6, 16, 1)

    private var certificate: Certificate? = null
    private var serverPublicKey: PublicKey? = null
    private var socket: OutputStream? = null

    var remote: RemoteManager? = null


//    var remote: ATVRemote1? = null


    val prefs = context.getSharedPreferences("tvHashnew", Context.MODE_PRIVATE)
    val tvHash = calculateImageHash(ip)

    init {

        Log.i("ATVRemote", ": ${prefs.contains(tvHash)}")
        if (!prefs.contains(tvHash)) {
            scope.launch {
                start()
                connectionListener.onPinRequired(6)
            }
        } else {
            setRemote()
        }
    }

    var isConnected = false

    fun setRemote() {
        // remote = ATVRemote1(sslContext, ip)
        //  remote?.execute(r = callBack, notConnected = notConnected)
        remote = RemoteManager(ip, 6466, sslContext!!)
        //remote?.start()

        remote?.setEventListener(object : RemoteManager.RemoteEventListener {
            override fun onReady() {
                if (!isConnected) {
                    connectionListener.onConnect()
                    isConnected = true
                }
                val tvHash = calculateImageHash(ip)
                if (!prefs.contains(tvHash)) {
                    prefs.edit { putBoolean(tvHash, true) }
                }
            }

            override fun onCurrentApp(packageName: String) {

            }

            override fun onVolumeChanged(volume: RemoteManager.VolumeInfo) {

            }

            override fun onPowered(powered: Boolean) {

            }

            override fun onError(error: Throwable) {
                Log.i("ATVRemote", "onError: ${error.message}")
                connectionListener.onFailedToConnect(error.message ?: "Failed to connect")
            }

            override fun onUnpaired() {
                connectionListener.onDisconnect()
                isConnected = false
            }
        })
        remote?.start()
    }

    suspend fun start() {
        Log.i("ATVRemote", "start: ")

        certificate = ATVSSLContext.getCertificate()
        try {
            Log.i("ATVRemote", "start: ")
            withContext(Dispatchers.IO) {
                val s = sslContext?.socketFactory?.createSocket(ip, PAIRING_PORT) as SSLSocket
                s.startHandshake()
                serverPublicKey = s.session.peerCertificates[0].publicKey
                socket = s.outputStream

                socket?.write(byteArrayOf(PAIRING_MSG.size.toByte()))
                socket?.write(PAIRING_MSG)

                socket?.write(byteArrayOf(OPTION_MSG.size.toByte()))
                socket?.write(OPTION_MSG)

                socket?.write(byteArrayOf(CONFIGURATION_MSG.size.toByte()))
                socket?.write(CONFIGURATION_MSG)
            }
        } catch (e: IOException) {
            Log.i("ATVRemote", "start: ${e.message}")
            e.printStackTrace()
        }

        //   presenter.pairingManagerSetupFinished()
    }

    fun finishPairing(code: String) {
        scope.launch {
            val one = PAIRING_MSG_HEADER
            val two: ByteArray?
            try {
                two = computeAlphaValue(code)
            } catch (e: CertificateException) {
                Log.i("ATVRemote", "finishPairing: cer eror ${e.message}")
                e.printStackTrace()
                return@launch
            }

            try {
                val combined = ByteArray(one.size + (two?.size ?: 0))

                for (i in combined.indices) {
                    combined[i] =
                        ((if (i < one.size) one[i] else two?.get(i - one.size)) ?: 0)
                }
                socket?.write(combined)
                socket?.flush()
                socket?.close()

                // remote = ATVRemote1(sslContext, ip)
                //  remote?.execute(r = onConnected)
                setRemote()
                Log.i("ATVRemote", "finishPairing: set")

            } catch (e: IOException) {
                Log.i("ATVRemote", "finishPairing: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private fun computeAlphaValue(text: String): ByteArray? {
        val certificate = certificate as? X509Certificate ?: return null

        Log.i("certificate", "computeAlphaValue: " + (true))
        val publicKey = certificate.publicKey
        val publicKey2 = serverPublicKey
        if (publicKey !is RSAPublicKey || publicKey2 !is RSAPublicKey) {
            Log.e("error", "Expecting RSA public key")
            return null
        }
        try {

            val instance = MessageDigest.getInstance("SHA-256")
            var bArr = BigInteger(text.substring(2), 16).toByteArray()
            bArr = removeLeadingNullBytes(bArr)
            val byteArray = publicKey.modulus.abs().toByteArray()
            val byteArray2 = publicKey.publicExponent.abs().toByteArray()
            val byteArray3 = publicKey2.modulus.abs().toByteArray()
            val byteArray4 = publicKey2.publicExponent.abs().toByteArray()
            val removeLeadingNullBytes = removeLeadingNullBytes(byteArray)
            val removeLeadingNullBytes2 = removeLeadingNullBytes(byteArray2)
            val removeLeadingNullBytes3 = removeLeadingNullBytes(byteArray3)
            val removeLeadingNullBytes4 = removeLeadingNullBytes(byteArray4)
            instance.update(removeLeadingNullBytes)
            instance.update(removeLeadingNullBytes2)
            instance.update(removeLeadingNullBytes3)
            instance.update(removeLeadingNullBytes4)
            instance.update(bArr)
            val a = instance.digest()
            instance.update(
                byteArrayOf(
                    1,
                    2,
                    3
                )
            ) // this has no meaning android studio is weird and forced me into this solution
            return a
        } catch (unused: NoSuchAlgorithmException) {
            Log.e("error", "no sha-256 implementation")
            return null
        }
    }

    private fun removeLeadingNullBytes(byteArray: ByteArray): ByteArray {
        var i = 0
        while (byteArray[i] == 0.toByte()) {
            i++
        }
        val arr = ByteArray(byteArray.size - i)
        var x = i
        var y = 0
        while (x < byteArray.size) {
            arr[y] = byteArray[x]
            x++
            y++
        }
        return arr
    }


    // Function to calculate the MD5 hash of the image file
    private fun calculateImageHash(file: String): String {
        val digest = MessageDigest.getInstance("MD5")
        val hashBytes = digest.digest(file.toByteArray())
        return Base64.encodeToString(hashBytes, Base64.DEFAULT)
    }

}
