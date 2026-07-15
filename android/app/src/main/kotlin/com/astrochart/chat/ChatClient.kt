package com.astrochart.chat

import com.astrochart.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Builds the [AnthropicApi] pointed at the chat proxy. The proxy URL and the
 * shared app-token come from [BuildConfig] (injected at build time — never
 * committed). When they are empty the app is "not configured" and the chat
 * screen offers guidance instead of calling the network.
 */
object ChatClient {

    val proxyUrl: String = BuildConfig.CHAT_PROXY_URL
    private val appToken: String = BuildConfig.CHAT_APP_TOKEN

    /**
     * True when both the proxy URL and the app token are set. The token is
     * required — without it the worker rejects every request with 401 — so an
     * app with only the URL is treated as not configured.
     */
    fun isConfigured(): Boolean = proxyUrl.isNotBlank() && appToken.isNotBlank()

    fun create(): AnthropicApi {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC
            else HttpLoggingInterceptor.Level.NONE
        }

        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("X-App-Token", appToken)
                    .header("content-type", "application/json")
                    .build()
                chain.proceed(request)
            }
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build()

        // Retrofit requires a base URL ending in "/"; tolerate a missing slash.
        val base = if (proxyUrl.endsWith("/")) proxyUrl else "$proxyUrl/"

        return Retrofit.Builder()
            .baseUrl(base)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AnthropicApi::class.java)
    }
}
