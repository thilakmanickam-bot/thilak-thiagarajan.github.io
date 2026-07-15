package com.astrochart.core.i18n

import java.util.Locale

/**
 * Languages the app can render content in. [code] is the persisted key and the
 * base for a java [Locale] used by date/number formatting; [displayName] is the
 * label shown in the language switcher (always in its own script).
 */
enum class Language(val code: String, val displayName: String) {
    EN("en", "English"),
    TA("ta", "தமிழ்"),
    ZH("zh", "中文");

    val locale: Locale get() = Locale(code)

    companion object {
        fun fromCode(code: String?): Language =
            entries.firstOrNull { it.code == code } ?: EN
    }
}
