package com.astrochart.core.i18n

import java.util.Locale

/**
 * Languages the app can be used in. [code] is the persisted key and the base for
 * a java [Locale]; [displayName] is the label shown in the language switcher
 * (always in its own script).
 *
 * Not every language yet has a full set of translated content. [content] maps a
 * language to the [ContentLang] whose strings it renders with, so a newly-added
 * language stays fully usable (falling back to English text) until its own
 * translation pack is filled in. EN / TA / ZH have complete packs and map to
 * themselves.
 */
enum class Language(val code: String, val displayName: String, val content: ContentLang) {
    EN("en", "English", ContentLang.EN),
    TA("ta", "தமிழ்", ContentLang.TA),
    HI("hi", "हिन्दी", ContentLang.EN),
    TE("te", "తెలుగు", ContentLang.EN),
    KN("kn", "ಕನ್ನಡ", ContentLang.EN),
    ML("ml", "മലയാളം", ContentLang.EN),
    MR("mr", "मराठी", ContentLang.EN),
    ZH("zh", "中文", ContentLang.ZH);

    val locale: Locale get() = Locale(code)

    companion object {
        fun fromCode(code: String?): Language =
            entries.firstOrNull { it.code == code } ?: EN
    }
}

/**
 * The set of languages that have a complete translated-content pack. Content
 * `when` expressions switch on [ContentLang] (via [Language.content]) so they
 * stay exhaustive as new display [Language]s are added.
 */
enum class ContentLang { EN, TA, ZH }
