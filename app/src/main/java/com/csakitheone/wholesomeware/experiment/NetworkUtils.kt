package com.csakitheone.wholesomeware.experiment

import java.security.KeyManagementException
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSession
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager


class NetworkUtils {
    class TrustAllCerts : X509TrustManager {
        override fun getAcceptedIssuers(): Array<X509Certificate?> {
            val myTrustedAnchors: Array<X509Certificate?> = arrayOfNulls<X509Certificate>(0)
            return myTrustedAnchors
        }

        public override fun checkClientTrusted(
            certs: Array<X509Certificate?>?,
            authType: String?
        ) {
        }

        public override fun checkServerTrusted(
            certs: Array<X509Certificate?>?,
            authType: String?
        ) {
        }
    }

    companion object {

        fun disableSSLCertificateVerify() {
            val trustAllCerts: Array<TrustManager?> =
                arrayOf<TrustManager>(object : X509TrustManager {
                    override fun getAcceptedIssuers(): Array<X509Certificate?> {
                        val myTrustedAnchors: Array<X509Certificate?> =
                            arrayOfNulls<X509Certificate>(0)
                        return myTrustedAnchors
                    }

                    public override fun checkClientTrusted(
                        certs: Array<X509Certificate?>?,
                        authType: String?
                    ) {
                    }

                    public override fun checkServerTrusted(
                        certs: Array<X509Certificate?>?,
                        authType: String?
                    ) {
                    }
                }
                ) as Array<TrustManager?>

            try {
                val sc = SSLContext.getInstance("SSL")

                sc.init(null, trustAllCerts, SecureRandom())
                HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory())
                HttpsURLConnection.setDefaultHostnameVerifier(object : HostnameVerifier {
                    override fun verify(hostname: String?, session: SSLSession?): Boolean {
                        return true
                    }
                })
            } catch (e: KeyManagementException) {
                e.printStackTrace()
            } catch (e: NoSuchAlgorithmException) {
                e.printStackTrace()
            }
        }

    }
}