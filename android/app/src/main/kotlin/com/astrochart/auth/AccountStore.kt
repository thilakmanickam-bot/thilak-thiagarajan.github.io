package com.astrochart.auth

import android.content.Context

/** The signed-in account as shown in the UI. */
data class Account(
    val uid: String,
    val displayName: String,
    val email: String,
    val photoUrl: String?
)

/**
 * Local cache of the last-known signed-in account, so the Account screen can show
 * who is signed in instantly without waiting on Firebase. Firebase Auth remains
 * the source of truth for the actual session; this is display-only. Mirrors the
 * shared-prefs pattern of [com.astrochart.ui.i18n.PrimaryProfileStore].
 */
object AccountStore {
    private const val PREFS = "astro_prefs"
    private const val KEY_UID = "account_uid"
    private const val KEY_NAME = "account_name"
    private const val KEY_EMAIL = "account_email"
    private const val KEY_PHOTO = "account_photo"

    fun load(context: Context): Account? {
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val uid = p.getString(KEY_UID, null) ?: return null
        return Account(
            uid = uid,
            displayName = p.getString(KEY_NAME, "") ?: "",
            email = p.getString(KEY_EMAIL, "") ?: "",
            photoUrl = p.getString(KEY_PHOTO, null)
        )
    }

    fun save(context: Context, account: Account) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_UID, account.uid)
            .putString(KEY_NAME, account.displayName)
            .putString(KEY_EMAIL, account.email)
            .putString(KEY_PHOTO, account.photoUrl)
            .apply()
    }

    fun clear(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_UID).remove(KEY_NAME).remove(KEY_EMAIL).remove(KEY_PHOTO)
            .apply()
    }
}
