package com.sdk.remote.androidremote.android

import android.content.Context
import android.os.Build
import android.util.Base64
import android.util.Log
import androidx.annotation.Keep
import androidx.core.content.edit
import com.google.protobuf.ByteString
import com.sdk.remote.androidremote.ATVSSLContext
import com.sdk.remote.androidremote.ConnectionListener
import com.sdk.remote.androidremote.android.remote.RemoteManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import pairing.Pairingmessage
import java.io.IOException
import java.io.OutputStream
import java.security.MessageDigest
import java.security.MessageDigest.getInstance
import java.security.PublicKey
import java.security.cert.Certificate
import java.security.cert.X509Certificate
import java.security.interfaces.RSAPublicKey
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocket

/**
 * Implements the Android TV Remote "pairing" handshake (the Polo protocol Google uses for
 * both Android TV and Google TV remote-control pairing) over the real generated protobuf
 * messages from pairingmessage.proto, instead of hand-built byte arrays. Every step's
 * acknowledgement is read back and validated, and the PIN math uses a plain hex decode
 * (not BigInteger, which silently drops/adds bytes around leading zero nibbles).
 */
@Keep
class PairingManager(
    private val context: Context,
    private val ip: String,
    private val connectionListener: ConnectionListener
) {

    val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val sslContext: SSLContext? = ATVSSLContext.getInstance(context)

    private val PAIRING_PORT = 6467

    // Purely cosmetic: shown to the user on the TV's pairing confirmation screen.
    private val CLIENT_NAME = Build.MODEL ?: "Android"
    private val SERVICE_NAME = "com.sdk.remote.androidremote/AndroidTvRemote"

    private var certificate: Certificate? = null
    private var serverPublicKey: PublicKey? = null
    private var sslSocket: SSLSocket? = null
    private var outputStream: OutputStream? = null
    private var readerJob: Job? = null
    private val responseChannel = Channel<Pairingmessage.PairingMessage>(Channel.UNLIMITED)

    var remote: RemoteManager? = null

    val prefs = context.getSharedPreferences("tvHashnew", Context.MODE_PRIVATE)
    val tvHash = calculateImageHash(ip)

    init {
        Log.i("ATVRemote", ": ${prefs.contains(tvHash)}")
        if (!prefs.contains(tvHash)) {
            scope.launch {
                if (start()) {
                    connectionListener.onPinRequired(6)
                }
            }
        } else {
            setRemote()
        }
    }

    var isConnected = false

    fun setRemote() {
        remote = RemoteManager(ip, 6466, sslContext!!)

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

            override fun onCurrentApp(packageName: String) {}

            override fun onVolumeChanged(volume: RemoteManager.VolumeInfo) {}

            override fun onPowered(powered: Boolean) {}

            override fun onError(error: Throwable) {
                if (!isConnected) {
                    Log.i("ATVRemote", "onError: ${error.message}")
                    connectionListener.onFailedToConnect(error.message ?: "Failed to connect")
                }
            }

            override fun onUnpaired() {
                connectionListener.onDisconnect()
                isConnected = false
            }
        })
        remote?.start()
    }

    /**
     * Opens the pairing socket and runs the request/option/configuration steps of the
     * handshake, validating the TV's acknowledgement after each one. Returns true once the
     * TV is ready for the user's PIN, false (with [ConnectionListener.onFailedToConnect]
     * already reported) if anything failed before that point.
     */
    suspend fun start(): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.i("ATVRemote", "start: ")
            certificate = ATVSSLContext.getCertificate()

            val socket = sslContext?.socketFactory?.createSocket(ip, PAIRING_PORT) as SSLSocket
            socket.startHandshake()
            serverPublicKey = socket.session.peerCertificates[0].publicKey
            sslSocket = socket
            outputStream = socket.outputStream
            startReadingResponses(socket)

            sendMessage(createPairingRequestMessage())
            waitForMessage()

            sendMessage(createPairingOptionMessage())
            waitForMessage()

            sendMessage(createConfigMessage())
            waitForMessage()

            true
        } catch (e: Exception) {
            Log.i("ATVRemote", "start: ${e.message}")
            connectionListener.onFailedToConnect(e.message ?: "Failed to connect")
            closeSocket()
            false
        }
    }

    fun finishPairing(code: String) {
        scope.launch {
            try {
                // The 6-hex-character code shown on the TV is [1 checksum byte][2 nonce
                // bytes]. The secret sent back is the full SHA-256 over
                // client-mod|client-exp|server-mod|server-exp|nonce, and the checksum byte
                // must equal that hash's first byte - this is Android TV Remote v2's actual
                // "Polo" challenge-response, matching Google's own reference implementation.
                if (code.length != 6) {
                    connectionListener.pinError("Invalid Pin Code")
                    closeSocket()
                    return@launch
                }

                val checksum = code.substring(0, 2).toInt(16)
                val nonce = hexStringToBytes(code.substring(2))
                val hash = computeAlpha(nonce)

                if (hash == null || (hash[0].toInt() and 0xFF) != checksum) {
                    connectionListener.pinError("Invalid Pin Code")
                    closeSocket()
                    return@launch
                }

                sendMessage(createSecretMessage(hash))
                waitForMessage()

                closeSocket()
                setRemote()
                Log.i("ATVRemote", "finishPairing: paired")
            } catch (e: Exception) {
                Log.i("ATVRemote", "finishPairing: ${e.message}")
                connectionListener.pinError("Invalid Pin Code")
                closeSocket()
            }
        }
    }

    // --- Protobuf message construction -------------------------------------------------

    private fun createPairingRequestMessage(): Pairingmessage.PairingMessage =
        Pairingmessage.PairingMessage.newBuilder()
            .setPairingRequest(
                Pairingmessage.PairingRequest.newBuilder()
                    .setClientName(CLIENT_NAME)
                    .setServiceName(SERVICE_NAME)
                    .build()
            )
            .setStatus(Pairingmessage.PairingMessage.Status.STATUS_OK)
            .setProtocolVersion(2)
            .build()

    private fun createPairingOptionMessage(): Pairingmessage.PairingMessage {
        val encoding = Pairingmessage.PairingEncoding.newBuilder()
            .setType(Pairingmessage.PairingEncoding.EncodingType.ENCODING_TYPE_HEXADECIMAL)
            .setSymbolLength(6)
            .build()

        return Pairingmessage.PairingMessage.newBuilder()
            .setPairingOption(
                Pairingmessage.PairingOption.newBuilder()
                    .setPreferredRole(Pairingmessage.RoleType.ROLE_TYPE_INPUT)
                    .addInputEncodings(encoding)
                    .build()
            )
            .setStatus(Pairingmessage.PairingMessage.Status.STATUS_OK)
            .setProtocolVersion(2)
            .build()
    }

    private fun createConfigMessage(): Pairingmessage.PairingMessage {
        val encoding = Pairingmessage.PairingEncoding.newBuilder()
            .setType(Pairingmessage.PairingEncoding.EncodingType.ENCODING_TYPE_HEXADECIMAL)
            .setSymbolLength(6)
            .build()

        return Pairingmessage.PairingMessage.newBuilder()
            .setPairingConfiguration(
                Pairingmessage.PairingConfiguration.newBuilder()
                    .setClientRole(Pairingmessage.RoleType.ROLE_TYPE_INPUT)
                    .setEncoding(encoding)
                    .build()
            )
            .setStatus(Pairingmessage.PairingMessage.Status.STATUS_OK)
            .setProtocolVersion(2)
            .build()
    }

    private fun createSecretMessage(secret: ByteArray): Pairingmessage.PairingMessage =
        Pairingmessage.PairingMessage.newBuilder()
            .setPairingSecret(
                Pairingmessage.PairingSecret.newBuilder()
                    .setSecret(ByteString.copyFrom(secret))
                    .build()
            )
            .setStatus(Pairingmessage.PairingMessage.Status.STATUS_OK)
            .setProtocolVersion(2)
            .build()

    // --- Wire I/O ------------------------------------------------------------------------

    private fun sendMessage(message: Pairingmessage.PairingMessage) {
        val stream = outputStream ?: throw IOException("Pairing socket is not open")
        message.writeDelimitedTo(stream)
        stream.flush()
    }

    private fun startReadingResponses(socket: SSLSocket) {
        readerJob = scope.launch {
            val input = socket.inputStream
            try {
                while (isActive) {
                    val message = Pairingmessage.PairingMessage.parseDelimitedFrom(input) ?: break
                    responseChannel.trySend(message)
                }
            } catch (e: IOException) {
                Log.i("ATVRemote", "pairing read loop ended: ${e.message}")
            }
        }
    }

    private suspend fun waitForMessage(): Pairingmessage.PairingMessage {
        val message = withTimeout(15_000) { responseChannel.receive() }
        if (message.status != Pairingmessage.PairingMessage.Status.STATUS_OK) {
            throw IOException("TV rejected pairing step with status ${message.status}")
        }
        return message
    }

    private fun closeSocket() {
        readerJob?.cancel()
        readerJob = null
        try {
            outputStream?.flush()
        } catch (_: IOException) {
        }
        try {
            sslSocket?.close()
        } catch (_: IOException) {
        }
        sslSocket = null
        outputStream = null
    }

    // --- PIN / challenge-response math ----------------------------------------------------
    // secret = SHA-256(client_modulus | client_exponent | server_modulus | server_exponent |
    // nonce), where "client" is our own cert (presented on the pairing TLS handshake) and
    // "server" is the TV's cert. The TV computes the same hash and only accepts pairing if
    // it matches.

    private fun computeAlpha(nonce: ByteArray): ByteArray? {
        val clientCert = certificate as? X509Certificate ?: return null
        val clientPublicKey = clientCert.publicKey
        val serverPub = serverPublicKey

        if (clientPublicKey !is RSAPublicKey || serverPub !is RSAPublicKey) {
            Log.e("ATVRemote", "computeAlpha: expecting RSA public keys")
            return null
        }

        return try {
            val digest: MessageDigest = getInstance("SHA-256")
            digest.update(removeLeadingNullBytes(clientPublicKey.modulus.abs().toByteArray()))
            digest.update(removeLeadingNullBytes(clientPublicKey.publicExponent.abs().toByteArray()))
            digest.update(removeLeadingNullBytes(serverPub.modulus.abs().toByteArray()))
            digest.update(removeLeadingNullBytes(serverPub.publicExponent.abs().toByteArray()))
            digest.update(nonce)
            digest.digest()
        } catch (e: Exception) {
            Log.e("ATVRemote", "computeAlpha: ${e.message}")
            null
        }
    }

    private fun removeLeadingNullBytes(byteArray: ByteArray): ByteArray {
        var offset = 0
        while (offset < byteArray.size && byteArray[offset] == 0.toByte()) {
            offset++
        }
        return byteArray.copyOfRange(offset, byteArray.size)
    }

    /** Plain, fixed-width hex decode - unlike BigInteger(hex,16).toByteArray(), this never
     *  drops or adds a byte around a leading zero nibble or a set high bit. */
    private fun hexStringToBytes(hex: String): ByteArray {
        require(hex.isNotEmpty() && hex.length % 2 == 0) { "Bad pin string." }
        return ByteArray(hex.length / 2) { i ->
            hex.substring(2 * i, 2 * i + 2).toInt(16).toByte()
        }
    }

    // Function to calculate the MD5 hash of the image file
    private fun calculateImageHash(file: String): String {
        val digest = MessageDigest.getInstance("MD5")
        val hashBytes = digest.digest(file.toByteArray())
        return Base64.encodeToString(hashBytes, Base64.DEFAULT)
    }
}
