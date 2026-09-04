package com.astrochart.auth

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.os.Build
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.astrochart.BuildConfig
import com.astrochart.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest

/**
 * Google sign-in via the modern Credential Manager ("Sign in with Google"),
 * exchanged for a Firebase Auth session. Firebase Auth is the source of truth for
 * the session; [AccountStore] only caches the display fields.
 *
 * Every entry point runs only when [com.astrochart.Features.AUTH_ENABLED] is true,
 * so with the placeholder `google-services.json` none of this executes.
 */
object AuthManager {

    private const val TAG = "AuthManager"

    /**
     * The runtime facts that decide whether Credential Manager can work at all,
     * formatted for a bug report. Reported alongside failures so a tester's
     * screenshot is enough to tell the layers apart without adb:
     *
     *  - `activityCtx=false` → the call got a non-Activity context and can
     *    never show its UI (Credential Manager requires an Activity).
     *  - `playServices=N` → [GoogleApiAvailability] status. 0 is SUCCESS;
     *    1 SERVICE_MISSING, 2 SERVICE_VERSION_UPDATE_REQUIRED,
     *    3 SERVICE_DISABLED, 9 SERVICE_INVALID, 18 SERVICE_UPDATING.
     *    Anything non-zero means the request can't be served on this device.
     *  - `certSha1` → the SHA-1 of the certificate *this installed build* is
     *    signed with, and `certRegistered` → whether that certificate is one of
     *    the Android OAuth clients in `google-services.json`. Google will only
     *    issue an ID token to a package/certificate pair it has registered, and
     *    a build's certificate depends on how it was produced (debug keystore,
     *    upload key, or Play's app-signing key), so this is the only way to know
     *    which certificate is actually in play on the device in hand. When it
     *    reads `false`, the printed SHA-1 is exactly what has to be added to the
     *    Firebase project (Project settings → Your apps → Add fingerprint).
     */
    fun environmentSummary(context: Context): String {
        val activityCtx = context.findActivity() != null
        val playServices = runCatching {
            GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context)
        }.getOrElse { -1 }
        val sha1 = signingCertSha1(context)
        val registered = sha1 != null && BuildConfig.REGISTERED_CERT_SHA1
            .split(",")
            .any { it.equals(sha1, ignoreCase = true) }
        return "activityCtx=$activityCtx playServices=$playServices " +
            "gms=${playServicesVersion(context)} " +
            "certSha1=${sha1 ?: "unknown"} certRegistered=$registered"
    }

    /**
     * Installed Play Services version. [GoogleApiAvailability] answers only
     * "can this device be served at all" — it returns SUCCESS for any version
     * at or above the one the app was built against, so two devices that both
     * report 0 can still be running Play Services builds years apart. When the
     * same artifact signs in on one device and hangs on another, the version
     * is one of the few things that actually differs between them, so report
     * it rather than collapsing it into that single status code.
     */
    private fun playServicesVersion(context: Context): String = runCatching {
        context.packageManager
            .getPackageInfo("com.google.android.gms", 0)
            .versionName ?: "unknown"
    }.getOrElse { "absent" }

    /**
     * SHA-1 of the APK's signing certificate, lowercase hex without separators —
     * the same value Firebase/Play Console call the certificate fingerprint, and
     * the same form `google-services.json` stores in `android_info.certificate_hash`.
     */
    private fun signingCertSha1(context: Context): String? = runCatching {
        val pm = context.packageManager
        val signatures: Array<Signature>? =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                pm.getPackageInfo(context.packageName, PackageManager.GET_SIGNING_CERTIFICATES)
                    .signingInfo
                    ?.apkContentsSigners
            } else {
                @Suppress("DEPRECATION")
                pm.getPackageInfo(context.packageName, PackageManager.GET_SIGNATURES).signatures
            }
        val certificate = signatures?.firstOrNull()?.toByteArray() ?: return@runCatching null
        MessageDigest.getInstance("SHA-1")
            .digest(certificate)
            .joinToString("") { "%02x".format(it) }
    }.getOrNull()

    private fun Context.findActivity(): Activity? {
        var current: Context = this
        while (current is ContextWrapper) {
            if (current is Activity) return current
            current = current.baseContext
        }
        return null
    }

    /** The currently signed-in account, or null. */
    fun currentAccount(): Account? = FirebaseAuth.getInstance().currentUser?.toAccount()

    /**
     * Launches the Google credential picker, exchanges the returned ID token for a
     * Firebase session, and returns the signed-in [Account]. Throws on cancellation
     * or failure (the caller surfaces a friendly message).
     */
    suspend fun signInWithGoogle(context: Context): Account {
        val webClientId = context.getString(R.string.default_web_client_id)
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(webClientId)
            .setAutoSelectEnabled(false)
            .build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        val response = try {
            CredentialManager.create(context).getCredential(context, request)
        } catch (t: Throwable) {
            // Credential Manager failures are environmental far more often than
            // they are user error (signing certificate not registered against
            // the Firebase project, Google provider disabled, no Play Services,
            // no account on device). The exception type and message name the
            // cause outright, so log it before the caller collapses it into a
            // friendly one-liner: `adb logcat -s AuthManager`.
            Log.e(TAG, "Credential Manager sign-in failed [${environmentSummary(context)}]", t)
            throw t
        }
        val credential = response.credential
        if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            val googleCredential = GoogleIdTokenCredential.createFrom(credential.data)
            val firebaseCredential = GoogleAuthProvider.getCredential(googleCredential.idToken, null)
            val result = FirebaseAuth.getInstance().signInWithCredential(firebaseCredential).await()
            return result.user?.toAccount()
                ?: throw IllegalStateException("Firebase returned no user after sign-in")
        }
        throw IllegalStateException("Unexpected credential type: ${credential.type}")
    }

    /** Ends the Firebase session and clears the Credential Manager selection state. */
    suspend fun signOut(context: Context) {
        FirebaseAuth.getInstance().signOut()
        runCatching {
            CredentialManager.create(context).clearCredentialState(ClearCredentialStateRequest())
        }
    }

    private fun FirebaseUser.toAccount() = Account(
        uid = uid,
        displayName = displayName ?: "",
        email = email ?: "",
        photoUrl = photoUrl?.toString()
    )
}
