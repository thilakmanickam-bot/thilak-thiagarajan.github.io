package com.astrochart.chat

import com.astrochart.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Builds the [AnthropicApi] pointed at the Halo chat proxy (see
 * `functions/src/index.ts`), authenticated with the signed-in user's Firebase
 * ID token. The proxy verifies that token, enforces the daily message cap,
 * and injects the real Anthropic API key server-side — the app never holds
 * or sends that key.
 */
object ChatClient {

    private const val ANTHROPIC_VERSION = "2023-06-01"

    fun create(idToken: String): AnthropicApi {
        val logging = HttpLoggingInterceptor().apply {
            // BASIC logs only method/URL/status — never headers, so the bearer
            // token is not written to logcat even in debug.
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC
            else HttpLoggingInterceptor.Level.NONE
        }

        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("Authorization", "Bearer $idToken")
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
            .baseUrl(BuildConfig.CHAT_PROXY_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AnthropicApi::class.java)
    }
}
