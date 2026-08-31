package com.astrochart.ui.i18n

import android.content.Context

/**
 * The person whose predictions the app foregrounds: their rasi seeds the Rasi
 * Palan hub and their daily reading drives the daily notification. Stored in the
 * shared app preferences. [rasi] is 0-based (0 = Aries) and [nakshatra] is
 * 0-based (0 = Ashwini).
 */
data class PrimaryProfile(val name: String, val rasi: Int, val nakshatra: Int)

object PrimaryProfileStore {
    private const val PREFS = "astro_prefs"
    private const val KEY_NAME = "primary_name"
    private const val KEY_RASI = "primary_rasi"
    private const val KEY_NAK = "primary_nak"

    /** The saved primary profile, or null if the user has not set one. */
    fun load(context: Context): PrimaryProfile? {
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val rasi = p.getInt(KEY_RASI, -1)
        val nak = p.getInt(KEY_NAK, -1)
        if (rasi !in 0..11 || nak !in 0..26) return null
        return PrimaryProfile(p.getString(KEY_NAME, "") ?: "", rasi, nak)
    }

    fun save(context: Context, profile: PrimaryProfile) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_NAME, profile.name)
            .putInt(KEY_RASI, profile.rasi)
            .putInt(KEY_NAK, profile.nakshatra)
            .apply()
    }

    fun clear(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_NAME).remove(KEY_RASI).remove(KEY_NAK)
            .apply()
    }
}
