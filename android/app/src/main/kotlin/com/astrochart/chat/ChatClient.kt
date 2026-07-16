package com.astrochart.chat

import com.astrochart.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Builds the [AnthropicApi] pointed straight at the Anthropic Messages API,
 * authenticated with the user's own API key (entered in-app, stored on-device
 * via [ApiKeyStore]). The key is attached as the `x-api-key` header on each
 * request and never sent anywhere but Anthropic.
 */
object ChatClient {

    private const val BASE_URL = "https://api.anthropic.com/"
    private const val ANTHROPIC_VERSION = "2023-06-01"

    fun create(apiKey: String): AnthropicApi {
        val logging = HttpLoggingInterceptor().apply {
            // BASIC logs only method/URL/status — never headers, so the key and
            // the x-api-key header are not written to logcat even in debug.
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC
            else HttpLoggingInterceptor.Level.NONE
        }

        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("x-api-key", apiKey)
                    .header("anthropic-version", ANTHROPIC_VERSION)
                    .header("content-type", "application/json")
                    .build()
                chain.proceed(request)
            }
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AnthropicApi::class.java)
    }
}
