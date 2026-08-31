package com.astrochart.ui.i18n

import android.content.Context
import com.astrochart.core.i18n.Language

/** Persists the chosen [Language] in SharedPreferences (no extra dependencies). */
object LanguageStore {
    private const val PREFS = "astro_prefs"
    private const val KEY_LANG = "language"

    fun load(context: Context): Language {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return Language.fromCode(prefs.getString(KEY_LANG, Language.EN.code))
    }

    /** True once the user has explicitly chosen a language (drives the first-launch picker). */
    fun hasChosen(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).contains(KEY_LANG)

    fun save(context: Context, language: Language) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LANG, language.code)
            .apply()
    }
}
