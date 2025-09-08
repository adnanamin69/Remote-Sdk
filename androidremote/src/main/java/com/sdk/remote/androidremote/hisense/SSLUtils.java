package com.sdk.remote.androidremote.hisense;

import android.content.Context;


import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class SSLUtils {
    private static char[] defaultKeystorePass(Context context) {
        // Fetch password via JNI and decode from Base64
        try {
            String b64 = com.sdk.remote.androidremote.RemoteNDKHelper.getHisenseBksPassword();
            String decoded = new String(android.util.Base64.decode(b64, android.util.Base64.DEFAULT));
            return decoded.toCharArray();
        } catch (Throwable t) {
            return "kraftwerk9".toCharArray();
        }
    }

    public static TrustManager createTrustAllTrustManager() {
        return new X509TrustManager() {
            public void checkClientTrusted(X509Certificate[] x509CertificateArr, String str) {
            }

            public void checkServerTrusted(X509Certificate[] x509CertificateArr, String str) {
            }

            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        };
    }

    public static SSLContext setupSSLContext(Context context, int i) {
        SSLContext sSLContext = null;
        try {
            TrustManager[] trustManagerArr = {createTrustAllTrustManager()};
            KeyStore keyStore = setupKeyStore(context, i);
            KeyManagerFactory instance = KeyManagerFactory.getInstance("X509");
            instance.init(keyStore, defaultKeystorePass(context));
            sSLContext = SSLContext.getInstance("TLS");
            sSLContext.init(instance.getKeyManagers(), trustManagerArr, new SecureRandom());
            return sSLContext;
        } catch (Exception unused) {
            return sSLContext;
        }
    }

    public static KeyStore setupKeyStore(Context context, int i) throws KeyStoreException, NoSuchProviderException, CertificateException, IOException, NoSuchAlgorithmException, UnrecoverableKeyException {
        KeyStore instance = KeyStore.getInstance("BKS", BouncyCastleProvider.PROVIDER_NAME);
        instance.load(context.getResources().openRawResource(i), defaultKeystorePass(context));
        return instance;
    }

}
