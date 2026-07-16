package com.astrochart.chat

import android.content.Context

/**
 * Persists the user's Anthropic API key in SharedPreferences (device-local, no
 * extra dependencies). The key is entered in-app and used to call the Messages
 * API directly; it never leaves the device except in the API request itself.
 */
object ApiKeyStore {
    private const val PREFS = "astro_prefs"
    private const val KEY_API = "anthropic_api_key"

    fun load(context: Context): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_API, "")
            .orEmpty()

    fun save(context: Context, key: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_API, key.trim())
            .apply()
    }
}
