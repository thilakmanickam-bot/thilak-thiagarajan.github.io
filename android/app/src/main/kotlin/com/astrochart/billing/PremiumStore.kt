package com.astrochart.billing

import android.content.Context

/**
 * Local cache of the caller's Halo Premium entitlement, as last verified by
 * the `verifyPurchase` Cloud Function. This is a **cache of a server-verified
 * fact**, not itself the source of truth — the source of truth is
 * `users/{uid}.premiumActive` in Firestore (see `functions/src/billing.ts`),
 * refreshed on each app launch via [BillingManager.refreshEntitlement]. A
 * device that's briefly offline still reads its last-known state from here.
 */
object PremiumStore {
    private const val PREFS = "astro_prefs"
    private const val KEY_ACTIVE = "premium_active"
    private const val KEY_EXPIRES_AT = "premium_expires_at"

    data class Entitlement(val active: Boolean, val expiresAtMillis: Long)

    fun load(context: Context): Entitlement {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return Entitlement(
            active = prefs.getBoolean(KEY_ACTIVE, false),
            expiresAtMillis = prefs.getLong(KEY_EXPIRES_AT, 0L)
        )
    }

    fun save(context: Context, active: Boolean, expiresAtMillis: Long) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_ACTIVE, active)
            .putLong(KEY_EXPIRES_AT, expiresAtMillis)
            .apply()
    }
}
