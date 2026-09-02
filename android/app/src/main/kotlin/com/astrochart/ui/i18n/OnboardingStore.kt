package com.astrochart.ui.i18n

import android.content.Context
import androidx.core.content.pm.PackageInfoCompat

/**
 * Tracks whether the welcome wizard ([com.astrochart.ui.screens.OnboardingWizard])
 * has been completed for the currently-installed app version. Stores the
 * versionCode it was last completed at (rather than a simple boolean, like
 * [LanguageStore.hasChosen]) so the wizard reappears once after an update too,
 * letting a returning user review their setup — prefilled from their existing
 * stores, not blank.
 */
object OnboardingStore {
    private const val PREFS = "astro_prefs"
    private const val KEY_COMPLETED_VERSION = "onboarding_completed_version"

    private fun currentVersionCode(context: Context): Long {
        val info = context.packageManager.getPackageInfo(context.packageName, 0)
        return PackageInfoCompat.getLongVersionCode(info)
    }

    /** True on first-ever launch, and again the first time a newer version runs. */
    fun shouldShow(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val completed = prefs.getLong(KEY_COMPLETED_VERSION, -1L)
        return completed < currentVersionCode(context)
    }

    fun markCompleted(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putLong(KEY_COMPLETED_VERSION, currentVersionCode(context))
            .apply()
    }
}
