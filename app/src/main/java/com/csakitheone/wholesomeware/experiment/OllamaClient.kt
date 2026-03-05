package com.csakitheone.wholesomeware.experiment

import android.util.Log
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class OllamaClient {
    companion object {

        private val okhttp = OkHttpClient.Builder()
            .callTimeout(30, TimeUnit.SECONDS)
            .build()
        private val homeNetworkHost = "80.99.231.215"
        private val myDeviceHost = "192.168.0.100"

        var workingHost: String? = null
            private set

        suspend fun isOllamaReachable(host: String): Boolean = suspendCancellableCoroutine { continuation ->
            val request = Request.Builder()
                .url("http://$host:11434/api/version")
                .get()
                .header("Content-Type", "application/json")
                .header("User-Agent", "WholesomeWareApp/1.0")
                .build()

            try {
                okhttp.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        workingHost = host
                        Log.d("OllamaClient", "Ollama reachable at $host")
                    }
                    continuation.resumeWith(Result.success(response.isSuccessful))
                }
            } catch (e: Exception) {
                Log.e("OllamaClient", "Error checking Ollama reachability at $host", e)
                continuation.resumeWith(Result.success(false))
            }
        }

        suspend fun findWorkingHost(): Boolean {
            // Try home network host first
            if (isOllamaReachable(homeNetworkHost)) {
                Log.d("OllamaClient", "Using home network host: $homeNetworkHost")
                return true
            }
            // Then try device host
            if (isOllamaReachable(myDeviceHost)) {
                Log.d("OllamaClient", "Using device host: $myDeviceHost")
                return true
            }
            return false
        }

        suspend fun generate(
            model: String,
            prompt: String,
            system: String = "",
            think: Boolean = false,
        ): String = suspendCancellableCoroutine { continuation ->
            try {
                val host = workingHost ?: myDeviceHost

                // Build JSON properly to avoid escaping issues
                val requestBody = JSONObject().apply {
                    put("model", model)
                    put("prompt", prompt)
                    put("system", system)
                    put("think", think)
                    put("stream", false)
                }.toString()

                val request = okhttp.newCall(
                    Request.Builder()
                        .url("http://$host:11434/api/generate")
                        .header("Content-Type", "application/json")
                        .header("User-Agent", "WholesomeWareApp/1.0")
                        .post(requestBody.toRequestBody("application/json".toMediaTypeOrNull()))
                        .build()
                )

                request.execute().use { res ->
                    if (!res.isSuccessful) {
                        continuation.resumeWith(Result.failure(Exception("Unexpected code $res")))
                        return@use
                    }
                    val response = JSONObject(res.body?.string() ?: throw Exception("Empty response body"))
                    val result = response.getString("response")
                    continuation.resumeWith(Result.success(result))
                }
            } catch (e: Exception) {
                continuation.resumeWith(Result.failure(e))
            }
        }

    }
}