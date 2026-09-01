package com.astrochart.auth

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.astrochart.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
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

        val response = CredentialManager.create(context).getCredential(context, request)
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
