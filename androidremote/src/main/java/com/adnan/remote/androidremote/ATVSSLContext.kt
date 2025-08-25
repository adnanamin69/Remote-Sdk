package com.adnan.remote.androidremote

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.annotation.Keep
import java.io.InputStream
import java.security.*
import java.security.cert.Certificate
import java.security.cert.X509Certificate
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import kotlin.jvm.java
import kotlin.text.toCharArray

@Keep
class ATVSSLContext private constructor() {

    companion object {
        private var instance: SSLContext? = null
        private var certificate: Certificate? = null

        fun getInstance(context: Context): SSLContext? {
            if (instance == null) {
                instance = createATVSSLContext(context)
            }
            return instance
        }

        fun getCertificate(): Certificate? {
            return certificate
        }

        private fun createATVSSLContext(context: Context): SSLContext? {
            return try {
                val sslContext = SSLContext.getInstance("TLS")
                val trustManagers = arrayOf<TrustManager>(
                    object : X509TrustManager {
                        override fun getAcceptedIssuers(): Array<X509Certificate>? {
                            return null
                        }

                        @SuppressLint("TrustAllX509TrustManager")
                        override fun checkClientTrusted(
                            arg0: Array<X509Certificate>,
                            arg1: String
                        ) {
                        }

                        override fun checkServerTrusted(
                            arg0: Array<X509Certificate>,
                            arg1: String
                        ) {
                        }
                    }
                )

                val keyStore = KeyStore.getInstance("PKCS12")

                val keyManagerFactory =
                    KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())

                val pfxFileB64 = YourNDKHelper.getPfxFileName()
                val pfxPassB64 = YourNDKHelper.getPfxPassword()
                val aliasB64 = YourNDKHelper.getPfxAlias()

                val pfxFile =
                    String(android.util.Base64.decode(pfxFileB64, android.util.Base64.DEFAULT))
                val pfxPass =
                    String(android.util.Base64.decode(pfxPassB64, android.util.Base64.DEFAULT))
                val alias =
                    String(android.util.Base64.decode(aliasB64, android.util.Base64.DEFAULT))

                val keyStoreStream: InputStream = context.assets.open(pfxFile)
                keyStore.load(keyStoreStream, pfxPass.toCharArray())
                keyManagerFactory.init(keyStore, pfxPass.toCharArray())
                certificate = keyStore.getCertificate(alias)
                sslContext.init(keyManagerFactory.keyManagers, trustManagers, null)

                Log.i("certificate", "createATVSSLContext : $certificate ins : $instance")
                Log.i(
                    "certificate",
                    "createATVSSLContext pkg: ${ATVSSLContext::class.java.`package`?.name}"
                )

                sslContext
            } catch (e: Exception) {
                Log.e("certificate", "Error creating ATVSSLContext: ${e.message}", e)
                null
            }
        }
    }
}
