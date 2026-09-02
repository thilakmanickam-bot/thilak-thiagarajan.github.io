package com.astrochart.auth

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.astrochart.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

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
     * The two runtime facts that decide whether Credential Manager can work at
     * all, formatted for a bug report. Reported alongside failures — including
     * the timeout, where no exception exists to inspect — so a tester's
     * screenshot is enough to tell the layers apart without adb:
     *
     *  - `activityCtx=false` → the call got a non-Activity context and can
     *    never show its UI (Credential Manager requires an Activity).
     *  - `playServices=N` → [GoogleApiAvailability] status. 0 is SUCCESS;
     *    1 SERVICE_MISSING, 2 SERVICE_VERSION_UPDATE_REQUIRED,
     *    3 SERVICE_DISABLED, 9 SERVICE_INVALID, 18 SERVICE_UPDATING.
     *    Anything non-zero means the request can't be served on this device.
     */
    fun environmentSummary(context: Context): String {
        val activityCtx = context.findActivity() != null
        val playServices = runCatching {
            GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context)
        }.getOrElse { -1 }
        return "activityCtx=$activityCtx playServices=$playServices"
    }

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
