package com.astrochart.billing

import android.content.Context
import com.astrochart.BuildConfig
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Talks to the `redeemTesterCode` Cloud Function (`functions/src/tester.ts`).
 *
 * The code itself is never in this file, or anywhere else in the app. It lives
 * in Secret Manager and is compared server-side, so it can be rotated without
 * shipping a build and cannot be lifted out of a published APK. All this class
 * knows is how to hand a typed string to the server and report what came back.
 *
 * The redeem response is the same `{active, expiresAtMillis}` shape
 * `verifyPurchase` returns, and lands in the same [PremiumStore] cache, so a
 * redeemed tester is indistinguishable from a subscriber to every Premium gate
 * in the app.
 */
class TesterCodeClient(private val context: Context) {

    /** What the caller needs to show, one state per outcome. */
    sealed interface Result {
        data class Redeemed(val expiresAtMillis: Long) : Result
        /** Request recorded for the owner to review. */
        data object RequestReceived : Result
        /** The server rejected the code. */
        data object InvalidCode : Result
        /** Too many wrong attempts today. */
        data object TooManyAttempts : Result
        /** Not signed in — there is no ID token to authenticate with. */
        data object NotSignedIn : Result
        /** Network failure, or the endpoint is not deployed yet. */
        data object Unavailable : Result
    }

    suspend fun redeem(code: String): Result = withContext(Dispatchers.IO) {
        val token = idToken() ?: return@withContext Result.NotSignedIn
        val body = JSONObject().put("code", code.trim()).toString()

        val (status, payload) = post("redeem", body, token) ?: return@withContext Result.Unavailable
        when (status) {
            200 -> {
                val expiresAtMillis = JSONObject(payload).optLong("expiresAtMillis", 0L)
                // Cached here rather than left to the next launch: the screen
                // reports success immediately, and Premium must already be on
                // when the user navigates back to it.
                PremiumStore.save(context, active = true, expiresAtMillis = expiresAtMillis)
                Result.Redeemed(expiresAtMillis)
            }
            403 -> Result.InvalidCode
            429 -> Result.TooManyAttempts
            401 -> Result.NotSignedIn
            else -> Result.Unavailable
        }
    }

    suspend fun requestAccess(email: String): Result = withContext(Dispatchers.IO) {
        val token = idToken() ?: return@withContext Result.NotSignedIn
        val body = JSONObject()
            .put("email", email.trim())
            .put("appVersion", BuildConfig.VERSION_NAME)
            .toString()

        val (status, _) = post("request", body, token) ?: return@withContext Result.Unavailable
        when (status) {
            200 -> Result.RequestReceived
            401 -> Result.NotSignedIn
            else -> Result.Unavailable
        }
    }

    private suspend fun idToken(): String? =
        runCatching {
            FirebaseAuth.getInstance().currentUser?.getIdToken(false)?.await()?.token
        }.getOrNull()

    /** (status, body), or null if the request could not be made at all. */
    private fun post(path: String, body: String, token: String): Pair<Int, String>? =
        runCatching {
            // BASE_URL carries no trailing slash; tester.ts mounts /redeem and
            // /request on one express app.
            val request = Request.Builder()
                .url("${BuildConfig.TESTER_REDEEM_BASE_URL}/$path")
                .header("Authorization", "Bearer $token")
                .post(body.toRequestBody("application/json".toMediaType()))
                .build()
            CLIENT.newCall(request).execute().use { response ->
                response.code to response.body?.string().orEmpty()
            }
        }.getOrNull()

    private companion object {
        val CLIENT: OkHttpClient = OkHttpClient.Builder()
            .callTimeout(30, TimeUnit.SECONDS)
            .build()
    }
}
