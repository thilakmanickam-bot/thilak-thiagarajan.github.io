package com.astrochart.ui.i18n

import android.content.Context

/**
 * Which observances the user wants reminding about, keyed by
 * [com.astrochart.core.panchangam.MonthPanchangam.KEYS] — the same stable keys
 * the calendar groups its rows by, so a toggle survives a change of language,
 * of location, and of the display name of the observance.
 *
 * Only the *choice* lives here. Nothing schedules per-date work: the reminder
 * worker recomputes each month's dates from the panchangam and asks whether
 * today is one of them, so a toggle never has to be re-scheduled and a month
 * whose dates shift with the user's location needs no migration.
 */
object VrathamReminderStore {
    private const val PREFS = "astro_prefs"
    private const val KEY_ENABLED = "vratham_reminders"

    /**
     * The enabled keys. Copied out of SharedPreferences rather than returned
     * directly: the set that `getStringSet` hands back must not be mutated, and
     * its contents are undefined after a later edit.
     */
    fun enabled(context: Context): Set<String> =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getStringSet(KEY_ENABLED, emptySet())
            ?.toSet()
            ?: emptySet()

    fun isEnabled(context: Context, key: String): Boolean = key in enabled(context)

    fun setEnabled(context: Context, key: String, on: Boolean) {
        val next = enabled(context).toMutableSet()
        if (on) next.add(key) else next.remove(key)
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putStringSet(KEY_ENABLED, next)
            .apply()
    }
}
